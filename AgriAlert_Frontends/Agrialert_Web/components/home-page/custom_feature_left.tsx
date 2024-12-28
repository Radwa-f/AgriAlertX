import React from 'react';
import Image from "next/image";
import Link from "next/link";
import * as Craft from "@/components/craft";
import { Button } from "@/components/ui/button";
import { AlertTriangle, ArrowRight, ChevronRight, Shield } from "lucide-react";
import farmerImage2 from "@/public/farmer2.jpg"; // Updated image name

const ModernFeatureLeft = () => {
  return (
    <Craft.Section className="relative overflow-hidden py-12 p-10 m-8">
      {/* Subtle background elements */}
      <div className="absolute inset-0 bg-gradient-to-b from-transparent via-blue-50/30 to-transparent dark:via-blue-950/20" />
      
      <Craft.Container className="relative grid items-center md:grid-cols-2 md:gap-12">
        {/* Image container with modern styling */}
        <div className="relative h-[300px] md:h-[400px] mb-6 md:mb-0">
          <div className="absolute inset-0 bg-gradient-to-t from-blue-900/20 to-transparent rounded-xl z-10" />
          <div className="relative h-full overflow-hidden rounded-xl border border-white/10 shadow-xl transform md:hover:scale-[1.02] transition-all duration-700">
            <Image
              src={farmerImage2}
              alt="Système d'alerte et recommandations agricoles"
              className="object-cover object-center rounded-xl"
              fill
              priority
              sizes="(max-width: 768px) 100vw, 50vw"
            />
          </div>
          
          {/* Floating status card */}
          <div className="absolute bottom-4 left-4 z-20 bg-white/90 dark:bg-black/90 backdrop-blur-lg rounded-lg p-3 shadow-lg border border-white/20 transform hover:-translate-y-1 transition-transform">
            <div className="flex items-center gap-3">
              <div className="h-10 w-10 rounded-full bg-blue-100 dark:bg-blue-900/50 flex items-center justify-center">
                <Shield className="h-5 w-5 text-blue-600 dark:text-blue-400" />
              </div>
              <div>
                <p className="text-xs font-medium text-gray-600 dark:text-gray-300">Protection Active</p>
                <p className="text-xl font-bold text-blue-600 dark:text-blue-400">24/7</p>
              </div>
            </div>
          </div>
        </div>

        <div className="flex flex-col gap-6 py-4">
          {/* Badge */}
          <div className="inline-flex items-center w-fit px-3 py-1 rounded-full text-sm font-medium bg-blue-50 dark:bg-blue-900/50 text-blue-800 dark:text-blue-200 border border-blue-100 dark:border-blue-800">
            <span className="w-1.5 h-1.5 rounded-full bg-blue-500 mr-2 animate-pulse" />
            Alertes Intelligentes
          </div>

          {/* Heading with gradient */}
          <h3 className="!my-0 text-3xl font-bold bg-gradient-to-r from-blue-700 via-blue-600 to-blue-500 dark:from-blue-400 dark:via-blue-300 dark:to-blue-200 bg-clip-text text-transparent">
            Recommandations Personnalisées et Alertes Instantanées
          </h3>

          {/* Description with better typography */}
          <p className="text-base leading-relaxed text-gray-600 dark:text-gray-300">
            Agri-Alert transforme la gestion des risques agricoles grâce à un 
            système d'alerte intelligent et des recommandations sur mesure. 
            Nos algorithmes analysent en temps réel les données climatiques 
            locales pour vous fournir des conseils précis et opportuns.
          </p>

          {/* Feature list */}
          <div className="grid grid-cols-2 gap-2 mt-1">
            {[
              'Alertes en temps réel',
              'Conseils personnalisés',
              'Suivi 24/7',
              'Protection optimale'
            ].map((feature) => (
              <div key={feature} className="flex items-center gap-1.5 text-sm text-gray-600 dark:text-gray-300">
                <ChevronRight className="h-4 w-4 text-blue-500" />
                <span>{feature}</span>
              </div>
            ))}
          </div>

          {/* CTA buttons with modern styling */}
          <div className="flex items-center gap-3 mt-2">
            <Button 
              className="bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-500 hover:to-blue-400 transform hover:scale-105 transition-all"
              asChild
            >
              <Link href="/demo">
                <AlertTriangle className="mr-2 h-4 w-4" />
                Essayer Maintenant
              </Link>
            </Button>
            <Button 
              variant="ghost"
              className="group"
              asChild
            >
              <Link href="/solutions" className='group-hover:text-white'>
                <span className='hover:text-white'>Découvrir nos Solutions</span>
                <ArrowRight className="ml-2 h-4 w-4 transform group-hover:translate-x-1 text-white transition-transform" />
              </Link>
            </Button>
          </div>
        </div>
      </Craft.Container>
    </Craft.Section>
  );
};

export default ModernFeatureLeft;