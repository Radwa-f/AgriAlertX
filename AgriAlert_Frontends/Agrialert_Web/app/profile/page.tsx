"use client";

import React, { useState, useEffect } from "react";
import { Edit } from "lucide-react";
import { useUser } from "../context/UserContext";
import Select, { MultiValue } from "react-select";

// List of available crops (same as in the SignUp component)
const cropsOptions = [
  { value: 'Rice', label: 'Rice' },
  { value: 'Wheat', label: 'Wheat' },
  { value: 'Maize', label: 'Maize' },
  { value: 'Millets', label: 'Millets' },
  { value: 'Bajra', label: 'Bajra (Pearl Millet)' },
  { value: 'Pulses', label: 'Pulses (Kharif)' },
  { value: 'Lentil', label: 'Lentil (Rabi)' },
  { value: 'Oilseeds', label: 'Oilseeds' },
  { value: 'Groundnut', label: 'Groundnut' },
  { value: 'Sugarcane', label: 'Sugarcane' },
  { value: 'SugarBeet', label: 'Sugar beet' },
  { value: 'Cotton', label: 'Cotton' },
  { value: 'Tea', label: 'Tea' },
  { value: 'Coffee', label: 'Coffee' },
  { value: 'Cocoa', label: 'Cocoa' },
  { value: 'Rubber', label: 'Rubber' },
  { value: 'Jute', label: 'Jute' },
  { value: 'Flax', label: 'Flax' },
  { value: 'Coconut', label: 'Coconut' },
  { value: 'OilPalm', label: 'Oil-palm' },
  { value: 'Clove', label: 'Clove' },
  { value: 'BlackPepper', label: 'Black Pepper' },
  { value: 'Cardamom', label: 'Cardamom' },
  { value: 'Turmeric', label: 'Turmeric' },
];

type CropOption = {
  value: string;
  label: string;
};

export function ProfilePage() {
  const { user, setUser } = useUser();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
  });
  const [selectedCrops, setSelectedCrops] = useState<MultiValue<CropOption>>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (user) {
      setFormData({
        firstName: user.firstName || "",
        lastName: user.lastName || "",
        email: user.email || "",
        phoneNumber: user.phoneNumber || "",
      });

      // Assuming crops are part of the user data
      if (user.crops) {
        setSelectedCrops(user.crops.map((crop: string) => ({ value: crop, label: crop })));
      }
    }
  }, [user]);

  const handleInputChange = (e: { target: { name: any; value: any; }; }) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleFormSubmit = async (e: { preventDefault: () => void; }) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    
    try {
      const token = localStorage.getItem("JWTtoken");
      const response = await fetch("http://localhost:8087/api/v1/user/profile", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          firstName: formData.firstName,
          lastName: formData.lastName,
          phoneNumber: formData.phoneNumber,
          crops: selectedCrops.map(crop => crop.value), // Send the updated crops
        }),
      });
  
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Failed to update profile");
      }
  
      // Profile updated successfully, now fetch the updated user data
      const updatedUser = await response.json();
  
      // Assuming the backend also returns a new token
      const newToken = updatedUser.newToken; // New token is returned under the 'newToken' key
      if (newToken) {
        localStorage.setItem("JWTtoken", newToken); // Store the new token in localStorage
      }
  
      // Update the user context with the new user data
      setUser(updatedUser);
  
      // After update, reset the form data state with updated user details
      setFormData({
        firstName: updatedUser.firstName || "",
        lastName: updatedUser.lastName || "",
        email: updatedUser.email || "",
        phoneNumber: updatedUser.phoneNumber || "",
      });
  
      // Update the selected crops if available in the response
      if (updatedUser.crops) {
        setSelectedCrops(updatedUser.crops.map((crop: string) => ({ value: crop, label: crop })));
      }
  
      setIsEditing(false); // Close the form or reset the edit mode
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message || "An error occurred while updating the profile");
      } else {
        setError("An error occurred while updating the profile");
      }
    } finally {
      setLoading(false);
    }
  };
  
  
  

  const availableCrops = cropsOptions.filter(
    (crop) => !selectedCrops.some((selected) => selected.value === crop.value)
  );

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white shadow rounded-lg">
      <h1 className="text-2xl font-bold mb-6 text-gray-800">Profile</h1>

      {/* Profile Information */}
      <section className="mb-6">
        <h2 className="text-lg font-semibold mb-4">Update Profile Information</h2>

        <form onSubmit={handleFormSubmit} className="space-y-6">
          {error && <p className="text-red-500 text-sm">{error}</p>}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label
                htmlFor="firstName"
                className="block text-sm font-medium text-gray-700"
              >
                First Name
              </label>
              <input
                type="text"
                id="firstName"
                name="firstName"
                value={formData.firstName}
                onChange={handleInputChange}
                className="mt-1 block w-full p-3 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                disabled={!isEditing}
              />
            </div>

            <div>
              <label
                htmlFor="lastName"
                className="block text-sm font-medium text-gray-700"
              >
                Last Name
              </label>
              <input
                type="text"
                id="lastName"
                name="lastName"
                value={formData.lastName}
                onChange={handleInputChange}
                className="mt-1 block w-full p-3 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                disabled={!isEditing}
              />
            </div>
          </div>

          <div>
            <label
              htmlFor="email"
              className="block text-sm font-medium text-gray-700"
            >
              Email
            </label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              className="mt-1 block w-full p-3 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
              disabled
            />
          </div>

          <div>
            <label
              htmlFor="phoneNumber"
              className="block text-sm font-medium text-gray-700"
            >
              Phone Number
            </label>
            <input
              type="text"
              id="phoneNumber"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleInputChange}
              className="mt-1 block w-full p-3 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
              disabled={!isEditing}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">
              Select Crops
            </label>
            <Select
              isMulti
              name="crops"
              options={availableCrops}
              value={selectedCrops}
              onChange={(newValue) => setSelectedCrops(newValue)}
              className="w-full mt-2"
              isDisabled={!isEditing}
            />
          </div>

          {isEditing && (
            <button
              type="submit"
              className={`w-full py-3 px-6 text-white font-semibold rounded-lg shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 ${loading ? "bg-gray-400 cursor-not-allowed" : "bg-blue-500 hover:bg-blue-600"}`}
              disabled={loading}
            >
              {loading ? "Saving..." : "Save Changes"}
            </button>
          )}
        </form>
      </section>

      <button
        onClick={() => setIsEditing((prev) => !prev)}
        className="text-sm text-blue-500 hover:underline flex items-center gap-1"
      >
        <Edit className="w-4 h-4" /> {isEditing ? "Cancel" : "Edit Profile"}
      </button>
    </div>
  );
}

export default ProfilePage;
