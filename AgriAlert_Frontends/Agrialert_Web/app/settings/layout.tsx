import { ReactNode } from "react";
// import { getServerSession } from "next-auth";
// import { authOptions } from "../utils/auth";
// import { redirect } from "next/navigation";
import Sidebar, { SidebarItem } from "../components/SideBar";
import { Home, Settings, User, FileText, HelpCircle ,BotMessageSquare} from "lucide-react"; 
import Navbar from "../components/NavBar";
import SharedLayout from "../components/SharedLayout";

export default async function HomeLayout({
  children,
}: {
  children: ReactNode;
}) {
  // const session = await getServerSession(authOptions);

  // if (!session) {
  //   return redirect("/login");
  // }

  // const userName = session.user?.name || "Guest";
  // const userEmail = session.user?.email || "example@example.com";

  return (
    <SharedLayout>{children}</SharedLayout>
  );
}