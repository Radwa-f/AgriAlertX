import React from 'react';
import Image from "next/image";
import Link from "next/link";
import Balancer from "react-wrap-balancer";
import { Section, Container } from "./craft";
import Logo from "@/public/logo.png";
import { Mail, MapPin, Phone, Github, Linkedin, Twitter } from 'lucide-react';

const Footer = () => {
  return (
    <footer className="not-prose relative border-t border-gray-200 dark:border-gray-800 p-10 ">
      <div className="absolute inset-0 bg-gradient-to-b from-transparent via-gray-50/30 to-transparent dark:via-gray-950/20" />
      
      <Section className="relative">
        <Container>
          <div className="grid gap-12 md:grid-cols-4">
            {/* Brand Column */}
            <div className="md:col-span-2 space-y-6">
              <Link href="/" className="block">
                <h3 className="sr-only">AgriAlert</h3>
                <Image
                  src={Logo}
                  alt="AgriAlert"
                  width={140}
                  height={32}
                  className="transition-all hover:opacity-75 dark:invert"
                />
              </Link>
              <p className="text-gray-600 dark:text-gray-300 max-w-md">
                <Balancer>
                  AgriAlert is a service that provides timely alerts and updates for agricultural activities, 
                  helping farmers make informed decisions through advanced technology and real-time monitoring.
                </Balancer>
              </p>
              <div className="flex gap-4">
                {[
                  { icon: <Github className="h-5 w-5" />, href: "#" },
                  { icon: <Linkedin className="h-5 w-5" />, href: "#" },
                  { icon: <Twitter className="h-5 w-5" />, href: "#" },
                ].map((social, index) => (
                  <Link
                    key={index}
                    href={social.href}
                    className="p-2 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300 
                             hover:bg-green-100 dark:hover:bg-green-900/30 hover:text-green-600 dark:hover:text-green-400 
                             transition-all duration-300"
                  >
                    {social.icon}
                  </Link>
                ))}
              </div>
            </div>

            {/* Quick Links */}
            <div className="space-y-4">
              <h4 className="text-sm font-semibold text-gray-900 dark:text-gray-100">Quick Links</h4>
              <ul className="space-y-3">
                {['About Us', 'Services', 'Solutions', 'Contact'].map((item) => (
                  <li key={item}>
                    <Link 
                      href={`/${item.toLowerCase().replace(' ', '-')}`}
                      className="text-gray-600 dark:text-gray-400 hover:text-green-600 dark:hover:text-green-400 transition-colors"
                    >
                      {item}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>

            {/* Contact Info */}
            <div className="space-y-4">
              <h4 className="text-sm font-semibold text-gray-900 dark:text-gray-100">Contact</h4>
              <ul className="space-y-3">
                {[
                  { icon: <Mail className="h-4 w-4" />, text: "contact@agrialert.com" },
                  { icon: <Phone className="h-4 w-4" />, text: "+1 (555) 123-4567" },
                  { icon: <MapPin className="h-4 w-4" />, text: "123 Agri Street, Farm City" },
                ].map((item, index) => (
                  <li key={index} className="flex items-center gap-2 text-gray-600 dark:text-gray-400">
                    {item.icon}
                    <span>{item.text}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          {/* Bottom Section */}
          <div className="mt-12 pt-8 border-t border-gray-200 dark:border-gray-800">
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
              <div className="flex flex-wrap gap-6 text-sm text-gray-600 dark:text-gray-400">
                {['Privacy Policy', 'Terms of Service', 'Cookie Policy'].map((item) => (
                  <Link
                    key={item}
                    href={`/${item.toLowerCase().replace(' ', '-')}`}
                    className="hover:text-green-600 dark:hover:text-green-400 transition-colors"
                  >
                    {item}
                  </Link>
                ))}
              </div>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                © {new Date().getFullYear()}{" "}
                <a 
                 
                  className="hover:text-green-600 dark:hover:text-green-400 transition-colors"
                >
                  AgriAlert
                </a>
                . All rights reserved.
              </p>
            </div>
          </div>
        </Container>
      </Section>
    </footer>
  );
};

export default Footer;