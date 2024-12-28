"use client";

import { usePathname } from "next/navigation";
import { Bell, Search } from "lucide-react";
import UserNav from "./UserNav";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogTrigger } from "@/components/ui/dialog";

interface LinkProps {
  name: string;
  href: string;
}

const links: LinkProps[] = [
  { name: "Home", href: "/home" },
  { name: "TV Shows", href: "/home/shows" },
  { name: "Movies", href: "/home/movies" },
  { name: "Recently Added", href: "/home/recently" },
  { name: "My List", href: "/home/user/list" },
];

export default function Navbar() {
  const pathName = usePathname();

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 backdrop-blur-md bg-white/70 shadow-sm">
      <div className="w-full max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
        {/* Left Section - Logo/Brand (Placeholder) */}
        <div className="flex items-center">
          <div className="text-2xl font-bold text-primary-500 tracking-tight">
            AgriAlert
          </div>
        </div>

        {/* Center Section - Navigation Links */}
        {/* <div className="hidden md:flex items-center space-x-6">
          {links.map((link) => (
            <a
              key={link.href}
              href={link.href}
              className={`
                text-sm font-medium transition-colors duration-200
                ${pathName === link.href 
                  ? "text-primary-600 underline" 
                  : "text-gray-600 hover:text-primary-500"}
              `}
            >
              {link.name}
            </a>
          ))}
        </div> */}

        {/* Right Section - Actions */}
        <div className="flex items-center space-x-4">
          {/* Search Dialog */}
          <Dialog>
            <DialogTrigger asChild>
              <button className="text-gray-600 hover:text-primary-500 transition-colors">
                <Search className="h-5 w-5" />
              </button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-md">
              <div className="flex items-center space-x-2">
                <Input 
                  type="text" 
                  placeholder="Search..." 
                  className="flex-grow"
                />
                <button className="bg-primary-500 text-white px-4 py-2 rounded-md">
                  Search
                </button>
              </div>
            </DialogContent>
          </Dialog>

          {/* Notifications */}
          <button className="relative text-gray-600 hover:text-primary-500 transition-colors">
            <Bell className="h-5 w-5" />
            <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center">
              3
            </span>
          </button>

          {/* User Navigation */}
          <UserNav />
        </div>
      </div>
    </nav>
  );
}