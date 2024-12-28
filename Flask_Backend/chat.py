from flask import Flask, request, jsonify
from transformers import pipeline

# Initialize the Hugging Face pipeline for text2text-generation
pipe = pipeline("text2text-generation", model="mrSoul7766/AgriQBot")

app = Flask(__name__)

# Define the API route
@app.route('/chat', methods=['POST'])
def chat():
    user_query = request.json.get('query')  # Get the query from the request
    if not user_query:
        return jsonify({'error': 'No query provided'}), 400

    # Generate the response using the model
    response = pipe(f"Q: {user_query}", max_length=256)

    # Return the response as JSON
    return jsonify({'response': response[0]['generated_text']})

# Ensure safe importing
if __name__ == "__main__":
    app.run(debug=True, use_reloader=False)
