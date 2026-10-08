<div align="center">

<img src="./src/main/resources/static/images/logo.png"
     alt="SahlDarbak Logo"
     width="280">

# SahlDarbak | سهل دربك

### Plan Smarter, Travel Together ✈️

**SahlDarbak** is a smart travel-planning and traveler-matching web platform that helps travelers discover suitable destinations, build personalized trips, and connect with other travelers throughout their journey.

</div>

---

## About SahlDarbak

Planning a trip often requires travelers to move between multiple platforms to search for destinations, cities, hotels, restaurants, activities, transportation, weather, and other travel information.

**SahlDarbak** brings these needs together into one personalized travel-planning experience.

The platform considers the traveler's preferences, budget, travel dates, travel style, food preferences, and restrictions to provide recommendations and organize the trip from the early planning stage until the traveler arrives at the destination.

SahlDarbak is a **travel planning and assistance platform**. It does not provide in-app booking or payment services.

---

## Problem

Travel planning can be time-consuming and fragmented.

Travelers often face challenges such as:

- Searching across multiple platforms for travel information.
- Deciding which destination best matches their interests, budget, and travel dates.
- Choosing which cities to visit during a multi-city trip.
- Finding suitable hotels, restaurants, and activities.
- Considering food preferences and travel restrictions.
- Manually organizing activities into a daily itinerary.
- Estimating the overall cost of a trip.
- Preparing for weather conditions and public holidays.
- Choosing suitable transportation between planned places.
- Knowing what to pack for the trip.
- Finding other travelers with similar interests while abroad.

---

## Solution

SahlDarbak provides one smart platform that supports travelers throughout the travel-planning journey.

The platform combines traveler preferences, trip details, artificial intelligence, and external travel data to create a more personalized and organized experience.

SahlDarbak helps travelers:

- Discover destinations that match their preferences.
- Compare destination options.
- Plan single-city and multi-city trips.
- Generate personalized daily itineraries.
- Discover hotels, restaurants, activities, and other places.
- Access halal-related restaurant information.
- Customize an accepted trip plan.
- Estimate trip expenses.
- Check weather and holiday information.
- Receive packing and transportation suggestions.
- Share selected travel information through Email and WhatsApp.
- Publish and explore travel experiences through the community.
- Check in while traveling.
- Discover nearby travelers with similar interests.
- Send and manage traveler match invitations.
- Chat with accepted travel matches.
- Block unwanted users.

The goal of SahlDarbak is to reduce the time and effort required to plan a trip while giving travelers more control over a personalized travel experience.

---

## Main Features

### Smart Destination Discovery
Uses traveler preferences, trip dates, budget, weather, and other travel information to help recommend suitable destinations.

### Personalized Travel Preferences
Travelers can define their preferred:

- Weather
- Environment
- Trip pace
- Crowd level
- Food preferences
- Activities
- Travel restrictions
- Travel type

### Single-City & Multi-City Planning
Travelers can plan a trip to one city or organize multiple cities across their travel dates.

### Smart Itinerary
Creates a personalized daily itinerary containing suggested places, restaurants, hotels, and activities.

### Trip Customization
After accepting an itinerary, travelers can add, update, or remove places from their trip plan.

### Budget Estimation
Provides an estimated trip budget covering major travel expense categories.

### Travel Preparation
Supports travelers with useful information such as:

- Weather
- Public holidays
- Packing suggestions
- Transportation suggestions

### Community
Travelers can publish and browse travel experiences and recommendations.

### Traveler Matching
Travelers can check in to their current destination and discover nearby travelers.

### Communication
The platform supports Email, WhatsApp, and in-site chat for selected travel features.

---

## Core User Flow

```mermaid
flowchart TD

    A[Register / Login]
    --> B[Create Travel Request]

    B --> C[Add Dates, Budget & Travel Type]

    C --> D[Set Travel Preferences]

    D --> E{Does the traveler know the destination?}

    E -->|No| F[AI Destination Recommendation]

    E -->|Yes| G[Select Destination]

    F --> G

    G --> H[Create Trip]

    H --> I{City Planning Mode}

    I -->|Single City| K[Smart Itinerary]

    I -->|Multi-City| J[Plan Trip Cities]

    J --> K

    K --> L[Review Itinerary]

    L --> M[Accept & Customize Trip Places]

    M --> N[Trip Budget & Travel Preparation]

    N --> O[Travel to Destination]

    O --> P[Check In]

    P --> Q[Explore Community & Nearby Travelers]

    Q --> R[TravelMatch & Chat]
```

---

# Class Diagram

The following diagram represents the **17 main entities** in SahlDarbak and their relationships.

```mermaid
classDiagram

    class User {
        +Integer id
        +String email
        +String phoneNumber
        +String password
        +LocalDate createdAt
    }

    class Profile {
        +Integer id
        +String fullName
        +LocalDate dateOfBirth
        +String gender
        +String country
        +String city
        +String bio
    }

    class TravelRequest {
        +Integer id
        +LocalDate startDate
        +LocalDate endDate
        +Double budget
        +String travelType
        +Integer groupSize
        +Integer adultsCount
        +String status
        +String cityPlanMode
    }

    class GeneralPreference {
        +Integer id
        +String weather
        +String environment
        +String crowdPreference
        +String tripPace
    }

    class FoodPreference {
        +Integer id
        +String foodType
        +Boolean isRequired
    }

    class ActivityPreference {
        +Integer id
        +String activityType
        +Integer priority
    }

    class TravelRestriction {
        +Integer id
        +String restrictionType
        +String description
        +Boolean isRequired
    }

    class Child {
        +Integer id
        +Integer age
    }

    class Trip {
        +Integer id
        +String country
        +String city
        +String status
    }

    class TripCity {
        +Integer id
        +String city
        +LocalDate startDate
        +LocalDate endDate
        +Integer cityOrder
        +String status
    }

    class Itinerary {
        +Integer id
        +String status
        +LocalDateTime generatedAt
        +String planJson
    }

    class TripPlace {
        +Integer id
        +String placeType
        +String name
        +String city
        +String officialWebsite
        +LocalDate scheduledAt
        +String notes
    }

    class TripBudgetEstimate {
        +Integer id
        +Double flightEstimate
        +Double accommodationEstimate
        +Double foodEstimate
        +Double transportationEstimate
        +Double activitiesEstimate
        +Double totalEstimate
        +String currency
        +String summary
        +LocalDateTime generatedAt
    }

    class TravelPresence {
        +Integer id
        +String country
        +String city
        +LocalDate checkedInAt
        +Double latitude
        +Double longitude
    }

    class TravelMatch {
        +Integer id
        +String message
        +String status
        +LocalDate createdAt
    }

    class BlockedUser {
        +Integer id
        +LocalDate blockedAt
    }

    class CommunityPost {
        +Integer id
        +String title
        +String content
        +String country
        +String city
        +Integer rating
    }


    User "1" --> "0..1" Profile : has

    User "1" --> "0..*" TravelRequest : creates

    User "1" --> "0..*" Trip : owns

    User "1" --> "0..1" TravelPresence : checks in

    User "1" --> "0..*" CommunityPost : publishes


    TravelRequest "1" --> "0..1" GeneralPreference : has

    TravelRequest "1" --> "0..*" FoodPreference : has

    TravelRequest "1" --> "0..*" ActivityPreference : has

    TravelRequest "1" --> "0..*" TravelRestriction : has

    TravelRequest "1" --> "0..*" Child : includes

    TravelRequest "1" --> "0..1" Trip : creates


    Trip "1" --> "0..*" TripCity : contains

    Trip "1" --> "0..1" Itinerary : has

    Trip "1" --> "0..1" TripBudgetEstimate : has

    Trip "1" --> "0..1" TravelPresence : active presence


    Itinerary "1" --> "0..*" TripPlace : contains


    User "1" --> "0..*" TravelMatch : sender

    User "1" --> "0..*" TravelMatch : receiver


    User "1" --> "0..*" BlockedUser : blocker

    User "1" --> "0..*" BlockedUser : blocked user
```

---

## Team

| Team Member | Main Area |
|---|---|
| **Razan Almadan** | Discover |
| **Lama Alharbi** | Plan |
| **Mohammed Aljubaili** | Explore |

---

<div align="center">

### SahlDarbak | سهل دربك ✈️

**Plan Smarter, Travel Together**

</div>
