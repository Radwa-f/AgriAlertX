"use client"
import { ReactNode, useEffect, useState } from "react";
import Sidebar, { SidebarItem } from "../components/SideBar";
import Navbar from "../components/NavBar";
import { 
  Home, 
  Settings, 
  User, 
  HelpCircle, 
  BotMessageSquare 
} from "lucide-react";
import { usePathname } from "next/navigation";

// Define the structure of the JWT payload
interface UserInfo {
  firstName?: string;
  lastName?: string;
  email?: string;
}

export default function SharedLayout({ children }: { children: ReactNode }) {
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const pathname = usePathname();

  // Fetch token from localStorage and decode it on the client side (if needed)
  useEffect(() => {
    const token = localStorage.getItem("JWTtoken");
    if (token) {
      try {
        const decoded = JSON.parse(atob(token.split(".")[1])) as UserInfo;
        setUserInfo(decoded);
      } catch (error) {
        console.error("Failed to decode token:", error);
      }
    }
  }, []);

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar */}
      <Sidebar 
        name={`${userInfo?.firstName} ${userInfo?.lastName}`} 
        email={userInfo?.email || ""}
      >
        <SidebarItem 
          icon={<Home className="text-gray-600" />} 
          text="Home" 
          href="/home"
          active={pathname === "/home"} 
        />
        <SidebarItem 
          icon={<User className="text-gray-600" />} 
          text="Profile" 
          href="/profile"
          active={pathname === "/profile"}
        />
        <SidebarItem 
          icon={<BotMessageSquare className="text-gray-600" />} 
          text="ChatBot" 
          href="/chatbot"
          active={pathname === "/chatbot"}
        />
        <SidebarItem 
          icon={<Settings className="text-gray-600" />} 
          text="Settings" 
          href="/settings"
          active={pathname === "/settings"}
        />
        <SidebarItem 
          icon={<HelpCircle className="text-gray-600" />} 
          text="Help" 
          href="/help"
          active={pathname === "/help"}
        />
      </Sidebar>

      {/* Main content area with top padding to account for fixed navbar */}
      <div className="flex flex-col flex-1 overflow-hidden">
        {/* Navbar - fixed positioning */}
        <Navbar />

        {/* Scrollable content area with top padding */}
        <main className="flex-1 overflow-y-auto pt-20 px-6 bg-white">
          {/* Add a container for better content spacing */}
          <div className="max-w-7xl mx-auto">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
}