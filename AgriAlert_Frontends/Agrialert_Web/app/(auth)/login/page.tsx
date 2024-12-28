"use client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation"; // To redirect after login

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(""); 
  const router = useRouter();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();

    const data = { email, password };

    try {
      const response = await fetch("http://localhost:8087/api/v1/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });

      if (response.ok) {
        // Get JWT token and store it in localStorage
        const token = await response.text();
        localStorage.setItem("JWTtoken", token);
        router.push("/home"); // Redirect to home
      } else {
        // Handle failed login attempt
        const errorData = await response.text();
        setError(errorData || "Login failed");
      }
    } catch (error) {
      setError("An error occurred during login");
      console.error("Login error:", error);
    }
  };

  return (
    <div className="mt-24  bg-white py-10 px-6 md:mt-0 md:max-w-sm md:px-14 ">
      <form onSubmit={handleLogin}>
        <h1 className="text-3xl font-semibold text-black">Log in</h1>
        <div className="space-y-4 mt-5">
          <Input
            type="email"
            name="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Email"
            className="bg-gray-100 placeholder:text-xs placeholder:text-gray-500 text-black w-full"
          />
          <Input
            type="password"
            name="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Password"
            className="bg-gray-100 placeholder:text-xs placeholder:text-gray-500 text-black w-full"
          />
          <Button
            type="submit"
            variant="default"
            className="w-full text-white"
          >
            Log in
          </Button>
        </div>
        {error && (
          <div className="text-red-500 text-sm mt-2">{error}</div> 
        )}
      </form>

      <div className="text-gray-700 text-sm mt-2">
        New to AgriAlert ?{" "}
        <Link className="text-blue-600 hover:underline" href="/signup">
          Sign up now
        </Link>
      </div>
      <div className="text-center mt-4">
              <a
                className="text-sm text-blue-500 hover:underline cursor-pointer"
                onClick={() => router.push("/forgot-password")}
              >
                Forgot your password ?
              </a>
      </div>
    </div>
  );
}
