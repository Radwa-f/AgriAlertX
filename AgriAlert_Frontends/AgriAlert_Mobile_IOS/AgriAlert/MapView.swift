import SwiftUI
import MapKit
import UIKit


struct MapViewWithMarker: UIViewRepresentable {
    let initialCoordinate: CLLocationCoordinate2D
    @Binding var selectedCoordinate: CLLocationCoordinate2D

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.showsUserLocation = true // Show user's current location
        mapView.userTrackingMode = .follow // Automatically focus on the user's location

        // Set the initial region
        let region = MKCoordinateRegion(
            center: initialCoordinate,
            span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
        )
        mapView.setRegion(region, animated: true)

        // Add an initial marker
        addMarker(to: mapView, at: initialCoordinate)

        return mapView
    }

    func updateUIView(_ uiView: MKMapView, context: Context) {
        // Center the map on the selected coordinate
        let region = MKCoordinateRegion(
            center: selectedCoordinate,
            span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
        )
        uiView.setRegion(region, animated: true)

        // Update the marker's position
        uiView.removeAnnotations(uiView.annotations) // Remove old annotations
        addMarker(to: uiView, at: selectedCoordinate)
    }

    func addMarker(to mapView: MKMapView, at coordinate: CLLocationCoordinate2D) {
        let marker = MKPointAnnotation()
        marker.coordinate = coordinate
        mapView.addAnnotation(marker)
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, MKMapViewDelegate {
        var parent: MapViewWithMarker

        init(_ parent: MapViewWithMarker) {
            self.parent = parent
        }

        func mapView(_ mapView: MKMapView, didUpdate userLocation: MKUserLocation) {
            // Update the map to center on the user's location if needed
            guard parent.selectedCoordinate.latitude == 0, parent.selectedCoordinate.longitude == 0 else { return }
            let userCoordinate = userLocation.coordinate
            parent.selectedCoordinate = userCoordinate
            let region = MKCoordinateRegion(
                center: userCoordinate,
                span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
            )
            mapView.setRegion(region, animated: true)
        }
    }
}





struct SearchBar: UIViewRepresentable {
    @Binding var text: String
    var onSearch: (String) -> Void

    class Coordinator: NSObject, UISearchBarDelegate {
        @Binding var text: String
        var onSearch: (String) -> Void

        init(text: Binding<String>, onSearch: @escaping (String) -> Void) {
            _text = text
            self.onSearch = onSearch
        }

        func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
            onSearch(searchBar.text ?? "")
            searchBar.resignFirstResponder() // Dismiss the keyboard
        }

        func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
            text = searchText
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(text: $text, onSearch: onSearch)
    }

    func makeUIView(context: Context) -> UISearchBar {
        let searchBar = UISearchBar()
        searchBar.delegate = context.coordinator
        searchBar.placeholder = "Search for a location"
        return searchBar
    }

    func updateUIView(_ uiView: UISearchBar, context: Context) {
        uiView.text = text
    }
}
