"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";

// Define the structure of your JWT payload including location
interface Location {
  latitude: number;
  longitude: number;
}

interface UserInfo {
  id?: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string,
  roles?: string;
  location?: Location;
  crops?: [] 
}

interface UserContextType {
  user: UserInfo | null;
  setUser: (user: UserInfo | null) => void;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export const UserProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserInfo | null>(null);

  useEffect(() => {
    // Check if there's a JWT token in localStorage
    const token = localStorage.getItem("JWTtoken");

    if (token) {
      try {
        const decodedUser = jwtDecode<UserInfo>(token);
        setUser(decodedUser); // Set the decoded user data in state
      } catch (error) {
        console.error("Failed to decode token:", error);
      }
    }
  }, []); // Runs once when the component mounts

  return (
    <UserContext.Provider value={{ user, setUser }}>
      {children}
    </UserContext.Provider>
  );
};

// Custom hook to use the UserContext in any component
export const useUser = (): UserContextType => {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error("useUser must be used within a UserProvider");
  }
  return context;
};
