"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Switch } from "@/components/ui/switch";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { 
  Bell, 
  Shield, 
  Globe, 
  Moon, 
  User,
  HelpCircle, 
  KeyRound,
  LogOut,
  Trash2,
  ChevronRight
} from "lucide-react";

export default function SettingsPage() {
  const router = useRouter();
  const [darkMode, setDarkMode] = useState(false);
  const [emailNotifs, setEmailNotifs] = useState(true);
  const [pushNotifs, setPushNotifs] = useState(true);
  const [locationAccess, setLocationAccess] = useState(false);
  const [loading, setLoading] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  // Handle account deletion
  const handleDeleteAccount = async () => {
    setLoading(true);
    try {
      const response = await fetch("http://localhost:8087/api/v1/user/delete", {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("JWTtoken")}`,
        },
      });

      if (response.ok) {
        // Clear JWT token
        localStorage.removeItem("JWTtoken");

        // Redirect to login page
        router.push("/login");
      } else {
        alert("Failed to delete account. Please try again.");
      }
    } catch (error) {
      console.error("Error deleting account:", error);
      alert("An error occurred. Please try again.");
    } finally {
      setLoading(false);
      setShowDeleteModal(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      <h1 className="text-3xl font-bold">Settings</h1>
      
      <div className="grid gap-6">
        {/* Profile Section */}
        <Card className="cursor-pointer hover:shadow-md transition-shadow" 
              onClick={() => router.push('/profile')}>
          <CardHeader>
            <div className="flex justify-between items-center">
              <div className="flex items-center gap-2">
                <User className="w-5 h-5" />
                <CardTitle>Profile Settings</CardTitle>
              </div>
              <ChevronRight className="w-5 h-5 text-gray-400" />
            </div>
            <CardDescription>
              Manage your profile information and preferences
            </CardDescription>
          </CardHeader>
        </Card>

        {/* Notifications & Permissions */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Bell className="w-5 h-5" />
              <CardTitle>Notifications & Permissions</CardTitle>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex justify-between items-center">
              <div>
                <p className="font-medium">Email Notifications</p>
                <p className="text-sm text-gray-500">Receive email updates about your crops</p>
              </div>
              <Switch checked={emailNotifs} onCheckedChange={setEmailNotifs} />
            </div>
            <div className="flex justify-between items-center">
              <div>
                <p className="font-medium">Push Notifications</p>
                <p className="text-sm text-gray-500">Get instant alerts on your device</p>
              </div>
              <Switch checked={pushNotifs} onCheckedChange={setPushNotifs} />
            </div>
            <div className="flex justify-between items-center">
              <div>
                <p className="font-medium">Location Access</p>
                <p className="text-sm text-gray-500">Allow access to your location for personalized features</p>
              </div>
              <Switch checked={locationAccess} onCheckedChange={setLocationAccess} />
            </div>
          </CardContent>
        </Card>

        {/* Appearance */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Moon className="w-5 h-5" />
              <CardTitle>Appearance</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <div className="flex justify-between items-center">
              <div>
                <p className="font-medium">Dark Mode</p>
                <p className="text-sm text-gray-500">Toggle dark mode theme</p>
              </div>
              <Switch checked={darkMode} onCheckedChange={setDarkMode} />
            </div>
          </CardContent>
        </Card>

        {/* Danger Zone */}
        <Card className="border-red-200">
          <CardHeader>
            <div className="flex items-center gap-2 text-red-600">
              <Trash2 className="w-5 h-5" />
              <CardTitle>Danger Zone</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <Button variant="destructive" className="w-full" onClick={() => setShowDeleteModal(true)} disabled={loading}>
              {loading ? "Deleting..." : "Delete Account"}
            </Button>
          </CardContent>
        </Card>
      </div>

      {/* Delete Confirmation Modal */}
      <Dialog open={showDeleteModal} onOpenChange={setShowDeleteModal}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Account</DialogTitle>
          </DialogHeader>
          <p>Are you sure you want to delete your account? This action cannot be undone.</p>
          <DialogFooter>
            <Button variant="secondary" onClick={() => setShowDeleteModal(false)} disabled={loading}>Cancel</Button>
            <Button variant="destructive" onClick={handleDeleteAccount} disabled={loading}>{loading ? "Deleting..." : "Delete"}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
