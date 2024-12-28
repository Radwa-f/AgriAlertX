"use client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Link from "next/link";
import GithubSignInButton from "@/app/components/GithubSigninButton";
import GoogleSignInButton from "@/app/components/GoogleSigninButton";
import { useState, useEffect } from "react";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import Select, { MultiValue } from "react-select";

// List of available crops
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

export default function SignUp() {
  const [selectedCrops, setSelectedCrops] = useState<MultiValue<CropOption>>([]);
  const [location, setLocation] = useState<{ latitude: number; longitude: number } | null>(null);

  // Automatically request geolocation when the component mounts
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLocation({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
          });
        },
        (error) => {
          toast.error("Unable to retrieve your location. Please enable location access.");
        }
      );
    } else {
      toast.error("Geolocation is not supported by this browser.");
    }
  }, []);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    if (!location) {
      toast.error("Location permission is required for registration.");
      return;
    }

    const formData = new FormData(event.target as HTMLFormElement);
    const data = {
      email: formData.get("email"),
      firstName: formData.get("firstName"),
      lastName: formData.get("lastName"),
      phoneNumber: formData.get("phoneNumber"),
      password: formData.get("password"),
      crops: selectedCrops.map((crop) => crop.value),
      location: {
        latitude: location.latitude,
        longitude: location.longitude,
      },
    };

    try {
      const response = await fetch("http://localhost:8087/api/v1/registration", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });

      if (response.ok) {
        toast.success("An email has been sent to you!");
        // Optionally, redirect or take further action
      } else {
        const errorMessage = await response.text();
        toast.error(`Error: ${errorMessage || "Please try again."}`);
      }
    } catch (error) {
      toast.error("An error occurred. Please try again later.");
    }
  };

  return (
    <div className="mt-24 rounded bg-white/80 py-10 px-6 md:mt-0 md:max-w-sm md:px-14">
      <ToastContainer position="top-right" autoClose={5000} />
      <form onSubmit={handleSubmit}>
        <h1 className="text-3xl font-semibold text-light-green">Sign Up</h1>
        <div className="space-y-4 mt-5">
          <Input
            type="text"
            name="firstName"
            placeholder="First Name"
            className="bg-light-blue/20 placeholder:text-xs placeholder:text-gray-400 w-full inline-block"
          />
          <Input
            type="text"
            name="lastName"
            placeholder="Last Name"
            className="bg-light-blue/20 placeholder:text-xs placeholder:text-gray-400 w-full inline-block"
          />
          <Input
            type="email"
            name="email"
            placeholder="Email"
            className="bg-light-blue/20 placeholder:text-xs placeholder:text-gray-400 w-full inline-block"
          />
          <Input
            type="text"
            name="phoneNumber"
            placeholder="Phone Number"
            className="bg-light-blue/20 placeholder:text-xs placeholder:text-gray-400 w-full inline-block"
          />
          <Input
            type="password"
            name="password"
            placeholder="Password"
            className="bg-light-blue/20 placeholder:text-xs placeholder:text-gray-400 w-full inline-block"
          />

          <div className="mt-4">
            <label className="block text-sm font-medium text-gray-700">Select Crops</label>
            <Select
              isMulti
              name="crops"
              options={cropsOptions}
              value={selectedCrops}
              onChange={(newValue) => setSelectedCrops(newValue)}
              className="w-full mt-2"
            />
          </div>

          <Button
            type="submit"
            variant="destructive"
            className="w-full bg-light-green hover:bg-light-green/80"
          >
            Sign Up
          </Button>
        </div>
      </form>

      <div className="text-gray-500 text-sm mt-2">
        Already have an account?{" "}
        <Link className="text-light-blue hover:underline" href="/login">
          Log in now!
        </Link>
      </div>

      <div className="flex w-full justify-center items-center gap-x-3 mt-6">
        <GithubSignInButton />
        <GoogleSignInButton />
      </div>
    </div>
  );
}
