import { ReactNode } from "react";
import { NavBar } from "../../components/navbar";  // Adjust the import path as needed
import Footer from "@/components/footer";
import Image from "next/image";
import farmbg from "../../public/bg-farm.jpg";

export default function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <div className="relative min-h-screen w-full h-full">
      {/* Background image can be added here if needed */}
      <NavBar/>

      <main className="relative z-10 flex min-h-screen flex-col items-center justify-center px-4 pt-24 md:pt-32">
        <div className="w-full max-w-md space-y-8 bg-white dark:bg-zinc-900 p-8 rounded-xl shadow-lg relative z-20 mb-4">
          {children}
        </div>
        <div className="absolute inset-0 z-10 overflow-hidden">
          <Image
            src={farmbg}
            alt="Background"
            layout="fill"
            objectFit="cover"
            className="opacity-60"
          />
        </div>
      </main>

      <Footer/>
    </div>
  );
}