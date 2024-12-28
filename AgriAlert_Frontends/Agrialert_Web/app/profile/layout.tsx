"use client";
import { ReactNode, useEffect, useState } from "react";
import { jwtDecode, JwtPayload } from "jwt-decode";
import SharedLayout from "../components/SharedLayout";

// Define the structure of the JWT payload
interface UserInfo extends JwtPayload {
  id?: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  crops?: string[];
  roles?: string;
}

export default function MainLayout({ children }: { children: ReactNode }) {
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null); // Correct type

  // Fetch token from localStorage and decode it on the client side
  useEffect(() => {
    const token = localStorage.getItem("JWTtoken");
    if (token) {
      try {
        const decoded = jwtDecode<UserInfo>(token); // Decode the token with a generic type
        setUserInfo(decoded); // Store the decoded payload in state
      } catch (error) {
        console.error("Failed to decode token:", error);
      }
    }
  }, []);

  return (
    <SharedLayout>{children}</SharedLayout>
  );
}
