import SwiftUI
import Alamofire

struct ChatBotView: View {
    @State private var messages: [ChatMessage] = []
    @State private var userMessage: String = ""
    @State private var isLoading: Bool = false
    @State private var errorMessage: ChatError?

    var body: some View {
        VStack {
            // Chat Messages
            ScrollViewReader { scrollView in
                ScrollView {
                    LazyVStack(spacing: 10) {
                        ForEach(messages) { message in
                            HStack {
                                if message.sender == "User" {
                                    Spacer()
                                    Text(message.content)
                                        .padding()
                                        .background(Color.blue.opacity(0.2))
                                        .cornerRadius(10)
                                        .frame(maxWidth: UIScreen.main.bounds.width * 0.7, alignment: .trailing)
                                } else {
                                    Text(message.content)
                                        .padding()
                                        .background(Color.green.opacity(0.2))
                                        .cornerRadius(10)
                                        .frame(maxWidth: UIScreen.main.bounds.width * 0.7, alignment: .leading)
                                    Spacer()
                                }
                            }
                        }
                    }
                    .padding()
                }
                .onChange(of: messages.count) { _ in
                    scrollView.scrollTo(messages.last?.id, anchor: .bottom)
                }
            }

            Divider()

            // Message Input
            HStack {
                TextField("Enter your message...", text: $userMessage)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .frame(minHeight: 40)

                if isLoading {
                    ProgressView()
                } else {
                    Button(action: sendMessage) {
                        Image(systemName: "paperplane.fill")
                            .resizable()
                            .frame(width: 24, height: 24)
                            .padding(8)
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(16)
                    }
                }
            }
            .padding()
        }
        .toolbar {
            ToolbarItem(placement: .principal) { // Centered title
                Text("AgriBot Assistant")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(Color(hex: "#2c3e50"))
            }
        }
        .alert(item: $errorMessage) { error in
            Alert(title: Text("Error"), message: Text(error.message), dismissButton: .default(Text("OK")))
        }
    }

    private func sendMessage() {
        guard !userMessage.isEmpty else { return }
        let userMessageCopy = userMessage
        messages.append(ChatMessage(sender: "User", content: userMessageCopy))
        userMessage = ""
        isLoading = true

        ChatBotAPI.shared.getChatResponse(message: userMessageCopy) { result in
            DispatchQueue.main.async {
                isLoading = false
                switch result {
                case .success(let response):
                    print("DEBUG: Chatbot response received: \(response)")
                    messages.append(ChatMessage(sender: "Bot", content: response))
                case .failure(let error):
                    print("ERROR: Failed to communicate with chatbot: \(error.localizedDescription)")
                    errorMessage = ChatError(message: "Failed to connect to chatbot. Please try again.")
                }
            }
        }
    }
}
