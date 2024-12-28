"use client";
import { MoreVertical, ChevronLast, ChevronFirst } from "lucide-react";
import { useContext, createContext, useState, ReactNode } from "react";
import { useRouter } from "next/navigation"; // Import useRouter

const SidebarContext = createContext<{ expanded: boolean }>({ expanded: true });

interface SidebarProps {
  children: ReactNode;
  name: string;
  email: string;
}

export default function Sidebar({ children, name, email }: SidebarProps) {
  const [expanded, setExpanded] = useState(true);

  return (
    <aside className={`h-screen ${expanded ? 'w-64' : 'w-16'} transition-all bg-white border-r shadow-sm`}>
      <nav className="h-full flex flex-col">
        <div className="p-4 pb-2 flex justify-between items-center">
          <img
            src="https://img.logoipsum.com/243.svg"
            className={`overflow-hidden transition-all ${expanded ? "w-32" : "w-0"}`}
            alt="Logo"
          />
          <button
            onClick={() => setExpanded((curr) => !curr)}
            className="p-1.5 rounded-lg bg-gray-50 hover:bg-gray-100"
          >
            {expanded ? <ChevronFirst /> : <ChevronLast />}
          </button>
        </div>

        <SidebarContext.Provider value={{ expanded }}>
          <ul className="flex-1 px-3 overflow-y-auto">
            {children}
          </ul>
        </SidebarContext.Provider>

        <div className="border-t flex p-3">
          <img
            src={`https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=c7d2fe&color=3730a3&bold=true`}
            alt="User Avatar"
            className="w-10 h-10 rounded-md"
          />

          <div
            className={`flex justify-between items-center overflow-hidden transition-all ${
              expanded ? "ml-3" : "w-0"
            }`}
          >
            <div className="leading-4">
              <h4 className="font-semibold">{name}</h4>
              <span className="text-xs text-gray-600">{email}</span>
            </div>
          </div>
        </div>
      </nav>
    </aside>
  );
}

interface SidebarItemProps {
  icon: ReactNode;
  text: string;
  href?: string; // Add optional href for navigation
  active?: boolean;
  alert?: boolean;
}

export function SidebarItem({ icon, text, href = "/", active, alert }: SidebarItemProps) {
  const { expanded } = useContext(SidebarContext);
  const router = useRouter();

  const handleClick = () => {
    // Navigate to the specified href
    router.push(href);
  };

  return (
    <li
      onClick={handleClick}
      className={`relative flex items-center py-2 px-3 my-1 font-medium rounded-md cursor-pointer transition-colors group ${
        active
          ? "bg-gradient-to-tr from-indigo-200 to-indigo-100 text-indigo-800"
          : "hover:bg-indigo-50 text-gray-600"
      }`}
    >
      {icon}
      <span
        className={`overflow-hidden transition-all ${expanded ? "ml-3" : "w-0"}`}
      >
        {text}
      </span>
      {alert && (
        <div
          className={`absolute right-2 w-2 h-2 rounded bg-indigo-400 ${expanded ? "" : "top-2"}`}
        />
      )}

      {!expanded && (
        <div
          className={`absolute left-full rounded-md px-2 py-1 ml-6 bg-indigo-100 text-indigo-800 text-sm invisible opacity-20 -translate-x-3 transition-all group-hover:visible group-hover:opacity-100 group-hover:translate-x-0`}
        >
          {text}
        </div>
      )}
    </li>
  );
}