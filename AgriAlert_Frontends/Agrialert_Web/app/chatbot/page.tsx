"use client";

import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import { Send, Volume2, Loader2, Bot, User, RefreshCcw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Textarea } from "@/components/ui/textarea";

export function ChatBot() {
  const [messages, setMessages] = useState<{ role: string; content: string }[]>([]);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [speechEnabled, setSpeechEnabled] = useState(true);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!window.speechSynthesis) setSpeechEnabled(false);
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim()) return;

    setMessages((prev) => [...prev, { role: "user", content: input }]);
    setInput("");
    setIsLoading(true);

    try {
      const response = await axios.post("http://127.0.0.1:5000/chat", {
        query: input,
      });
      setMessages((prev) => [...prev, { role: "assistant", content: response.data.response }]);
    } catch (error) {
      console.error("Error:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const speak = (text: string) => {
    if (!speechEnabled) return;
  
    // Cancel any ongoing speech synthesis
    window.speechSynthesis.cancel();
  
    // Create a new SpeechSynthesisUtterance instance
    const utterance = new SpeechSynthesisUtterance(text);
  
    // Set the language to English (US)
    utterance.lang = "en-US";
  
    // When the speech ends, set isSpeaking to false
    utterance.onend = () => setIsSpeaking(false);
  
    // Set isSpeaking to true to indicate that speech is ongoing
    setIsSpeaking(true);
  
    // Speak the text
    window.speechSynthesis.speak(utterance);
  };
  

  return (
    <div className="flex flex-col h-[calc(100vh-80px)] -mt-4 bg-gradient-to-b from-gray-50 to-gray-100 rounded-lg shadow-lg">
      <div className="flex items-center justify-between p-4 border-b bg-white rounded-t-lg">
        <div className="flex items-center gap-2">
          <Bot className="w-6 h-6 text-blue-500" />
          <h1 className="text-xl font-semibold">AI Assistant</h1>
        </div>
        <Button
          variant="ghost"
          size="icon"
          onClick={() => setMessages([])}
        >
          <RefreshCcw className="w-4 h-4" />
        </Button>
      </div>

      <ScrollArea className="flex-1 px-4">
        <div className="space-y-4 py-4">
          {messages.length === 0 && (
            <div className="flex flex-col items-center justify-center h-64 text-gray-500">
              <Bot className="w-12 h-12 mb-4 text-gray-400" />
              <p className="text-lg font-medium">How can I help you today?</p>
              <p className="text-sm">Type a message to start the conversation</p>
            </div>
          )}
          
          {messages.map((msg, index) => (
            <div key={index} className={`flex items-start gap-3 ${msg.role === "user" ? "flex-row-reverse" : "flex-row"}`}>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${msg.role === "user" ? "bg-blue-500" : "bg-gray-700"}`}>
                {msg.role === "user" ? 
                  <User className="w-5 h-5 text-white" /> : 
                  <Bot className="w-5 h-5 text-white" />}
              </div>
              <Card className={`group relative max-w-[80%] ${msg.role === "user" ? "bg-blue-500 text-white border-none" : "bg-white"}`}>
                <div className="p-3 whitespace-pre-wrap">{msg.content}</div>
                {msg.role === "assistant" && speechEnabled && (
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => speak(msg.content)}
                    className="absolute -right-12 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity"
                    disabled={isSpeaking}
                  >
                    <Volume2 className="w-4 h-4" />
                  </Button>
                )}
              </Card>
            </div>
          ))}
          {isLoading && (
            <div className="flex items-start gap-3">
              <div className="w-8 h-8 rounded-full bg-gray-700 flex items-center justify-center">
                <Bot className="w-5 h-5 text-white" />
              </div>
              <Card className="p-3">
                <Loader2 className="w-4 h-4 animate-spin" />
              </Card>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>
      </ScrollArea>

      <form onSubmit={handleSubmit} className="p-4 border-t bg-white rounded-b-lg">
        <div className="flex gap-2">
          <Textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Type a message..."
            className="flex-1 min-h-[48px] max-h-[200px] resize-none"
            disabled={isLoading}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSubmit(e);
              }
            }}
          />
          <Button
            type="submit"
            size="icon"
            className="h-12 w-12"
            disabled={isLoading}
          >
            {isLoading ? 
              <Loader2 className="w-4 h-4 animate-spin" /> : 
              <Send className="w-4 h-4" />}
          </Button>
        </div>
      </form>
    </div>
  );
}

export default ChatBot;