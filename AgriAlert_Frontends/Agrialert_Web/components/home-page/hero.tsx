// React and Next.js imports
import Link from "next/link";
import Image from "next/image";

// Third-party library imports
import Balancer from "react-wrap-balancer";
import { AlertTriangle, CloudRain } from "lucide-react";

// Local component imports
import { Section, Container } from "@/components/craft";
import { Button } from "@/components/ui/button";

import logo from "@/public/logo.png";

export default function Hero() {
  return (
    <section className=" min-h-screen flex items-center justify-center overflow-hidden bg-gradient-to-b from-white to-green-50 dark:from-black dark:to-green-950/20">
      {/* Decorative background elements */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-green-200/30 dark:bg-green-900/20 rounded-full blur-3xl" />
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-blue-200/30 dark:bg-blue-900/20 rounded-full blur-3xl" />
      </div>
      
      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-32 flex flex-col items-center text-center">
        <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-green-100 dark:bg-green-900/50 text-green-800 dark:text-green-200 mb-8">
          🌱 Pour une agriculture résiliente
        </span>
        
        <h1 className="text-5xl md:text-6xl font-bold bg-gradient-to-r from-green-700 to-green-500 dark:from-green-400 dark:to-green-200 bg-clip-text text-transparent animate-fade-in">
          <Balancer>
            Protégez vos cultures contre les risques climatiques
          </Balancer>
        </h1>
        
        <h3 className="mt-6 text-xl text-gray-600 dark:text-gray-300 max-w-3xl">
          <Balancer>
            Une solution intelligente de surveillance et de prévention des catastrophes agricoles, 
            conçue pour renforcer la résilience des agriculteurs face aux changements climatiques.
          </Balancer>
        </h3>

        <div className="mt-12 flex flex-col sm:flex-row gap-4 sm:gap-6">
          <Button 
            size="lg"
            className="bg-gradient-to-r from-green-600 to-green-500 hover:from-green-500 hover:to-green-400 transform hover:scale-105 transition-all"
            asChild
          >
            <Link href="/alertes">
              <AlertTriangle className="mr-2 h-5 w-5" />
              Recevoir des Alertes
            </Link>
          </Button>
          
          <Button 
            size="lg"
            variant="outline"
            className="border-2 hover:bg-green-50 dark:hover:bg-green-950/30 transform hover:scale-105 transition-all"
            asChild
          >
            <Link href="/solutions">
              <CloudRain className="mr-2 h-5 w-5" />
              Nos Solutions
            </Link>
          </Button>
        </div>

        {/* Trust indicators */}
        <div className="mt-16 pt-8 border-t border-gray-200 dark:border-gray-800 flex flex-col items-center">
          <p className="text-sm text-gray-500 dark:text-gray-400 mb-4">Fait confiance par des milliers d'agriculteurs</p>
          {/* <div className="flex gap-8 items-center justify-center flex-wrap">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-12 w-32 bg-gray-200 dark:bg-gray-800 rounded-lg opacity-50" />
            ))}
          </div> */}
        </div>
      </div>
    </section>
  );
}

// export default Hero; (removed to avoid multiple default exports)