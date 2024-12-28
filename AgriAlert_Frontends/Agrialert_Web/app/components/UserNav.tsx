import { useEffect } from "react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { LogOut, User } from "lucide-react";
import { useUser } from "../context/UserContext";
import { useRouter } from "next/navigation";

export default function UserNav() {
  const { user, setUser } = useUser();
  const router = useRouter();

  const handleLogout = () => {
    localStorage.removeItem("JWTtoken");
    setUser(null);
    router.push("/login");
  };

  useEffect(() => {
    if (!user) {
      const token = localStorage.getItem("JWTtoken");
      if (!token) {
        router.push("/login");
      }
    }
  }, [user, router]);

  if (!user) {
    return null;
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button 
          variant="ghost" 
          className="relative h-8 w-8 rounded-full p-0 hover:bg-gray-100"
        >
          <Avatar className="h-8 w-8 rounded-full">
            <AvatarImage 
              src="https://nkljkwikhlggqlnjctge.supabase.co/storage/v1/object/public/user%20image/avatar.png" 
              alt="User avatar"
            />
            <AvatarFallback className="rounded-full text-white text-xs">
              {user?.firstName?.charAt(0)}
            </AvatarFallback>
          </Avatar>
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent 
        className="w-64 rounded-xl shadow-md border-none bg-white/90 backdrop-blur-sm p-2 space-y-2" 
        align="end"
        forceMount
      >
        <DropdownMenuLabel>
          <div className="flex items-center space-x-3">
            <Avatar className="h-10 w-10 rounded-full">
              <AvatarImage 
                src="https://nkljkwikhlggqlnjctge.supabase.co/storage/v1/object/public/user%20image/avatar.png" 
                alt="User avatar"
              />
              <AvatarFallback className="rounded-full text-white text-sm">
                {user?.firstName?.charAt(0)}
              </AvatarFallback>
            </Avatar>
            <div>
              <p className="text-sm font-medium">{`${user.firstName} ${user.lastName}`}</p>
              <p className="text-xs text-gray-500 truncate max-w-[180px]">{user.email}</p>
            </div>
          </div>
        </DropdownMenuLabel>
        
        <DropdownMenuSeparator className="bg-gray-200" />
        
        <DropdownMenuItem 
          onClick={() => router.push("/profile")}
          className="flex items-center space-x-2 hover:bg-gray-100 cursor-pointer rounded-md px-2 py-2 text-sm"
        >
          <User className="h-4 w-4 text-gray-600" />
          <span>Profile</span>
        </DropdownMenuItem>
        
        <DropdownMenuItem
          onClick={handleLogout}
          className="flex items-center space-x-2 text-red-500 hover:bg-red-100 cursor-pointer rounded-md px-2 py-2 text-sm"
        >
          <LogOut className="h-4 w-4" />
          <span>Sign out</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}