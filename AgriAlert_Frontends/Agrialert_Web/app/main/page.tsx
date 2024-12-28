"use client";

import React, { useEffect, useState } from "react";
import axios from "axios";
import { Loader2 } from "lucide-react";
import { useUser } from "../context/UserContext";

export function MainPage() {
  const [messages, setMessages] = useState<{ role: string; content: string }[]>([]);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const { user, setUser } = useUser(); // Access user data and setUser function

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInput(e.target.value);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim()) return;

    // Add user query to messages
    setMessages((prev) => [...prev, { role: "user", content: input }]);
    setInput("");
    setIsLoading(true);

    try {
      const response = await axios.post("http://127.0.0.1:5000/chat", {
        query: input,
      });

      // Add chatbot response to messages
      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: response.data.response },
      ]);
    } catch (error) {
      console.error("Error communicating with server:", error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-[80vh] w-full border rounded-md shadow-md bg-white overflow-hidden">
      <div className="flex-1 overflow-y-auto p-6 space-y-4">
        {messages.map((msg, index) => (
          <div
            key={index}
            className={`flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}
          >
            {msg.role === "assistant" && (
              <div className="flex-shrink-0 w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center mr-4">
                <span className="text-sm font-bold text-gray-600">AI</span>
              </div>
            )}
            <div
              className={`max-w-[60%] p-4 rounded-lg shadow-md text-base ${
                msg.role === "user"
                  ? "bg-blue-500 text-white rounded-br-none"
                  : "bg-gray-100 text-gray-800 rounded-bl-none"
              }`}
            >
              {msg.content}
            </div>
            {msg.role === "user" && (
              <div className="flex-shrink-0 w-12 h-12 rounded-full bg-blue-500 flex items-center justify-center ml-4">
              <span className="text-sm font-bold text-white">
                {user?.firstName?.charAt(0)}{user?.lastName?.charAt(0)}
              </span>
            </div>
            
            )}
          </div>
        ))}
        {isLoading && (
          <div className="flex justify-start">
            <div className="flex-shrink-0 w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center mr-4">
              <span className="text-sm font-bold text-gray-600">AI</span>
            </div>
            <div className="max-w-[60%] p-4 rounded-lg shadow-md text-base bg-gray-100 text-gray-800">
              <Loader2 className="animate-spin h-6 w-6 text-gray-400" />
            </div>
          </div>
        )}
      </div>
      <form onSubmit={handleSubmit} className="flex items-center p-4 border-t bg-gray-50">
        <input
          type="text"
          value={input}
          onChange={handleInputChange}
          className="flex-1 px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400 text-lg"
          placeholder="Type a message..."
          disabled={isLoading}
        />
        <button
          type="submit"
          className="ml-4 px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-300 text-lg"
          disabled={isLoading}
        >
          Send
        </button>
      </form>
    </div>
  );
}

export default MainPage;
