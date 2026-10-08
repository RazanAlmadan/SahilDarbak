<div align="center">

<img src="./src/main/resources/static/images/logo.png"
     alt="SahlDarbak Logo"
     width="280">

# SahlDarbak | سهل دربك

### Plan Smarter, Travel Together ✈️

**SahlDarbak** is a smart travel-planning and traveler-matching web platform that helps travelers discover suitable destinations, build personalized trips, and explore their journey with greater confidence.

</div>

---

## Project Brief

**SahlDarbak** is a smart travel platform designed to support travelers before and during their trips.

The platform combines traveler preferences, trip details, artificial intelligence, and external travel data to create a more personalized travel-planning experience.

It helps travelers move from choosing a destination to planning cities and daily activities, preparing for the trip, and exploring their destination after arrival.

SahlDarbak is a **travel planning and assistance platform** and does not provide in-app booking or payment services.

---

## Problem

Planning a trip often requires travelers to use multiple platforms for different needs.

Travelers may need to search separately for destinations, weather, cities, accommodation, restaurants, activities, transportation, budgets, and travel experiences.

This can make travel planning:

- Time-consuming and fragmented.
- Difficult to personalize.
- Harder when planning multiple cities.
- Complicated when considering budget, preferences, or restrictions.
- Less convenient for travelers who want to explore or connect with others after arriving.

---

## Solution

SahlDarbak brings the main stages of the travel journey into one platform.

The system uses traveler preferences, trip information, AI, and external data to help users:

- Discover suitable destinations.
- Build personalized travel plans.
- Organize single-city or multi-city trips.
- Generate and customize daily itineraries.
- Estimate trip expenses and prepare for travel.
- Explore destination-related community experiences.
- Access a location-based city guide after arrival.
- Discover and connect with nearby travelers.
- Receive selected travel information through Email and WhatsApp.

The goal is to reduce the effort required to plan a trip while providing travelers with a more organized and personalized experience.

---

## Main Features

- AI-powered destination recommendations and comparisons.
- Personalized travel planning based on preferences, budget, and travel dates.
- Single-city and multi-city trip planning.
- Smart daily itinerary generation and trip customization.
- Trip budget estimation and travel preparation support.
- Community posts and destination-related travel experiences.
- Location-based city guide after arrival.
- Traveler matching, invitations, and chat.
- Email and WhatsApp support for selected travel information.

---

## Core User Flow

```mermaid
flowchart LR

    A[Create Travel Request]
    --> B[Set Travel Preferences]

    B --> C[Choose or Recommend Destination]

    C --> D[Plan Cities & Trip]

    D --> E[Generate Smart Itinerary]

    E --> F[Customize & Prepare Trip]

    F --> G[Travel & Check In]

    G --> H[City Guide, Community & TravelMatch]
```

---

## Class Diagram

The following class diagram represents the **17 main entities** in SahlDarbak and their relationships.

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
    User "1" --> "0..1" TravelPresence : checks in
    User "1" --> "0..*" TravelRequest : creates
    User "1" --> "0..*" Trip : owns
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
    Trip "1" --> "0..1" TravelPresence : current presence

    Itinerary "1" --> "0..*" TripPlace : contains

    User "1" --> "0..*" TravelMatch : sends
    User "1" --> "0..*" TravelMatch : receives

    User "1" --> "0..*" BlockedUser : blocks
    User "1" --> "0..*" BlockedUser : is blocked
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
