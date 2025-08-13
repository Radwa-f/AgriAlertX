# evaluate.py
import argparse, json
from pathlib import Path
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import joblib

from sklearn.metrics import (
    accuracy_score, f1_score, classification_report,
    confusion_matrix, top_k_accuracy_score
)
from sklearn.model_selection import train_test_split, StratifiedKFold, cross_val_score
from sklearn.preprocessing import LabelEncoder

FEATURES = ["temperature", "humidity", "ph", "rainfall"]

def metrics_block(y_true_labels, y_pred_labels, y_proba, class_order, k_list=(3,)):
    """All label arrays are strings; class_order is the list of class names (strings)
       in the same order as y_proba columns."""
    out = {
        "accuracy": float(accuracy_score(y_true_labels, y_pred_labels)),
        "f1_macro": float(f1_score(y_true_labels, y_pred_labels, average="macro")),
        "per_class": classification_report(
            y_true_labels, y_pred_labels,
            labels=class_order, target_names=class_order,
            output_dict=True, zero_division=0
        ),
    }
    for k in k_list:
        try:
            out[f"top{k}_accuracy"] = float(
                top_k_accuracy_score(y_true_labels, y_proba, k=k, labels=class_order)
            )
        except Exception:
            out[f"top{k}_accuracy"] = None

    cm = confusion_matrix(y_true_labels, y_pred_labels, labels=class_order)
    cm_norm = cm.astype(float)
    row_sums = cm_norm.sum(axis=1, keepdims=True)
    cm_norm = np.divide(cm_norm, np.where(row_sums == 0, 1, row_sums))
    out["confusion_matrix"] = cm.tolist()
    out["confusion_matrix_normalized"] = cm_norm.tolist()
    return out, cm_norm

def plot_confusion(cm_norm, classes, out_png):
    plt.figure(figsize=(8,8))
    plt.imshow(cm_norm, interpolation="nearest")
    plt.title("Confusion Matrix (normalized)")
    plt.xticks(range(len(classes)), classes, rotation=90)
    plt.yticks(range(len(classes)), classes)
    plt.colorbar()
    plt.tight_layout()
    plt.savefig(out_png, dpi=160)
    plt.close()

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--data", default="Crop_recommendation.csv")
    ap.add_argument("--model", default="crop_suitability_lr.pkl")
    ap.add_argument("--out",   default="metrics.json")
    ap.add_argument("--assumed_ph", type=float, default=6.5)
    ap.add_argument("--test_size", type=float, default=0.2)
    ap.add_argument("--seed", type=int, default=42)
    args = ap.parse_args()

    df = pd.read_csv(args.data)
    assert set(FEATURES + ["label"]).issubset(df.columns), "Unexpected CSV schema"

    # Encode target to get a stable split; we’ll convert back to strings for metrics.
    le = LabelEncoder()
    y_idx = le.fit_transform(df["label"])
    classes_from_data = list(le.classes_)

    train_df, test_df, y_train_idx, y_test_idx = train_test_split(
        df, y_idx, test_size=args.test_size, random_state=args.seed, stratify=y_idx
    )

    # Load pipeline (StandardScaler + LogisticRegression)
    pipe = joblib.load(args.model)

    # Prefer the model’s class ordering for probabilities/metrics
    classes_model = list(getattr(pipe, "classes_", classes_from_data))

    # ---------- Evaluation with TRUE pH ----------
    X_test_with = test_df[FEATURES].copy()                 # keep column names
    y_true_labels = le.inverse_transform(y_test_idx)       # strings
    y_pred_labels = pipe.predict(X_test_with)              # strings
    y_proba = pipe.predict_proba(X_test_with)              # columns align with pipe.classes_

    m_with, cm_with = metrics_block(y_true_labels, y_pred_labels, y_proba, classes_model)
    plot_confusion(np.array(m_with["confusion_matrix_normalized"]), classes_model, "cm_with_ph.png")

    # ---------- Evaluation with ASSUMED pH (production-like) ----------
    X_test_no = test_df[["temperature", "humidity", "rainfall"]].copy()
    X_test_no["ph"] = args.assumed_ph
    X_test_no = X_test_no[FEATURES]                        # ensure correct column order
    y_pred_labels_no = pipe.predict(X_test_no)
    y_proba_no = pipe.predict_proba(X_test_no)

    m_no, cm_no = metrics_block(y_true_labels, y_pred_labels_no, y_proba_no, classes_model)
    plot_confusion(np.array(m_no["confusion_matrix_normalized"]), classes_model, "cm_assumed_ph.png")

    # ---------- Optional 5-fold CV (with true pH) ----------
    X_all = df[FEATURES].copy()
    y_all_labels = df["label"]  # strings
    results_cv = None
    try:
        skf = StratifiedKFold(n_splits=5, shuffle=True, random_state=args.seed)
        # cross_val_score needs y as array-like; strings are fine
        f1_cv = cross_val_score(pipe, X_all, y_all_labels, cv=skf, scoring="f1_macro")
        acc_cv = cross_val_score(pipe, X_all, y_all_labels, cv=skf, scoring="accuracy")
        results_cv = {
            "folds": 5,
            "f1_macro_mean": float(np.mean(f1_cv)),
            "f1_macro_std": float(np.std(f1_cv)),
            "accuracy_mean": float(np.mean(acc_cv)),
            "accuracy_std": float(np.std(acc_cv)),
        }
    except Exception:
        results_cv = None

    # ---------- Persist ----------
    metrics = {
        "notes": "Hold-out metrics and optional CV. Confusion matrices saved as PNG files.",
        "classes_model": classes_model,
        "assumed_ph": args.assumed_ph,
        "test_size": args.test_size,
        "seed": args.seed,
        "results": {
            "with_ph": m_with,
            "assumed_ph": m_no,
            "cross_validation": results_cv
        }
    }
    Path(args.out).write_text(json.dumps(metrics, indent=2))

    print(f"Wrote {args.out}")
    print("with_ph    -> acc={:.3f} f1_macro={:.3f}".format(
        metrics["results"]["with_ph"]["accuracy"],
        metrics["results"]["with_ph"]["f1_macro"]))
    print("assumed_ph -> acc={:.3f} f1_macro={:.3f}".format(
        metrics["results"]["assumed_ph"]["accuracy"],
        metrics["results"]["assumed_ph"]["f1_macro"]))
    if results_cv:
        print("5-fold CV  -> acc={:.3f}±{:.3f} f1={:.3f}±{:.3f}".format(
            results_cv["accuracy_mean"], results_cv["accuracy_std"],
            results_cv["f1_macro_mean"], results_cv["f1_macro_std"]))

if __name__ == "__main__":
    main()
