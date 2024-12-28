"use client";
import { ReactNode} from "react";
import SharedLayout from "../components/SharedLayout";


export default function Chatbot({ children }: { children: ReactNode }) {
  return (
    <SharedLayout>{children}</SharedLayout>
  );
}
