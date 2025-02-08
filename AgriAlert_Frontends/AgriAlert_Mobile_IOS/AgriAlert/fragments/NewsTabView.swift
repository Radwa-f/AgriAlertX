import SwiftUI

struct NewsTabView: View {
    @State private var newsList: [NewsItem] = []
    @State private var searchText: String = ""
    @State private var filteredNews: [NewsItem] = []

    var body: some View {
            VStack {
                ScrollView {
                    VStack(spacing: 16) { // Match spacing with CropsTabView
                        ForEach(filteredNews) { news in
                            NewsCardView(newsItem: news)
                                .padding(.horizontal, 16) // Match horizontal padding
                                .onTapGesture {
                                    if let urlString = news.url, let url = URL(string: urlString) {
                                        UIApplication.shared.open(url)
                                    }
                                }
                        }
                    }
                    .padding(.horizontal, 16) // Match outer horizontal padding
                }
            }
            .onAppear {
                fetchNews()
            }
        }

    func fetchNews() {
        NewsAPI.shared.fetchNews(query: "agriculture", country: "ma", limit: 10) { result in
            switch result {
            case .success(let news):
                DispatchQueue.main.async {
                    self.newsList = sortNews(news)
                    self.filteredNews = sortNews(news) // Initialize filtered list with sorted data
                }
            case .failure(let error):
                print("Error fetching news: \(error.localizedDescription)")
            }
        }
    }

    func filterNews(query: String) {
        if query.isEmpty {
            filteredNews = newsList
        } else {
            filteredNews = newsList.filter { news in
                news.title?.lowercased().contains(query.lowercased()) == true
            }
        }
    }

    func sortNews(_ news: [NewsItem]) -> [NewsItem] {
        return news.sorted { item1, item2 in
            let startsWithA1 = item1.title?.lowercased().hasPrefix("a") ?? false
            let startsWithA2 = item2.title?.lowercased().hasPrefix("a") ?? false

            if startsWithA1 && !startsWithA2 {
                return true
            } else if !startsWithA1 && startsWithA2 {
                return false
            }
            return (item1.title ?? "") < (item2.title ?? "")
        }
    }
}

struct NewsCardView: View {
    let newsItem: NewsItem

    var body: some View {
            VStack(alignment: .leading, spacing: 8) {
                // News Image
                AsyncImage(url: URL(string: newsItem.image ?? "")) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    Color(.systemGray5)
                }
                .frame(width: 330)
                .clipped()
                .cornerRadius(12)

                // News Title
                               Text(newsItem.title ?? "No Title")
                                   .font(.headline)
                                   .foregroundColor(.primary)
                                   .lineLimit(2)

                               // News Description
                               Text(truncatedDescription())
                                   .font(.subheadline)
                                   .foregroundColor(.secondary)
            }
            .padding()
            .background(Color.white)
            .cornerRadius(16)
            .shadow(color: Color.black.opacity(0.1), radius: 8, x: 0, y: 4)
        }

        // Function to split text into lines of up to 5 words
        private func splitTextIntoLines(_ text: String) -> String {
            let words = text.split(separator: " ")
            var lines: [String] = []
            var currentLine: [String] = []

            for word in words {
                currentLine.append(String(word))
                if currentLine.count == 5 {
                    lines.append(currentLine.joined(separator: " "))
                    currentLine = []
                }
            }

            // Add any remaining words to the last line
            if !currentLine.isEmpty {
                lines.append(currentLine.joined(separator: " "))
            }

            return lines.joined(separator: "\n")
        }
    
    func truncatedDescription() -> String {
        guard let text = newsItem.text else { return "No Description" }
        if let firstStopIndex = text.firstIndex(of: ".") {
            return String(text[..<firstStopIndex]) + "."
        }
        return text
    }
}

