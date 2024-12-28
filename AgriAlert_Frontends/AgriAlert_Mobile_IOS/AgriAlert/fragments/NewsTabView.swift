import SwiftUI

struct NewsTabView: View {
    @State private var newsList: [NewsItem] = []
    @State private var searchText: String = ""
    @State private var filteredNews: [NewsItem] = []

    var body: some View {
        VStack {
            // Search Bar
            TextField("Search", text: $searchText)
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(10)
                .padding(.horizontal)
                .onChange(of: searchText) { oldValue, newValue in
                    filterNews(query: newValue)
                }


            // News List
            ScrollView {
                VStack(spacing: 16) {
                    ForEach(filteredNews) { news in
                        NewsCardView(newsItem: news)
                            .onTapGesture {
                                if let urlString = news.url, let url = URL(string: urlString) {
                                    UIApplication.shared.open(url)
                                }
                            }
                    }
                }
                .padding(.horizontal, 20)
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
                    .scaledToFill()
            } placeholder: {
                Color(.systemGray4)
            }
            .frame(height: 200)
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
        .padding(.horizontal,10)
        .padding(.horizontal, 20)
        .padding(.top, 20)
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.1), radius: 8, x: 0, y: 4)
    }
    
    func truncatedDescription() -> String {
        guard let text = newsItem.text else { return "No Description" }
        if let firstStopIndex = text.firstIndex(of: ".") {
            return String(text[..<firstStopIndex]) + "."
        }
        return text
    }
}


