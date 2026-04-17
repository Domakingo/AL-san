# AL-san - How It Works

**AL-san** is an unofficial Android app for [AniList](https://anilist.co). It's a fork of [AL-chan](https://github.com/zend10/AL-chan) that I'm maintaining as a personal project.

---

## The Basics

AL-san is built with **Kotlin** and follows the **MVVM** pattern. Here's the tech I use:

- **Koin** for dependency injection
- **RxJava3** for reactive data handling
- **Apollo 3** for talking to AniList's GraphQL API
- **Retrofit** for other REST APIs (YouTube, Spotify, etc.)
- **Coil** for loading images
- **Material Design** for the UI
- **Markwon** for high-performance Markdown rendering
- **WorkManager** for background tasks and sync
- **Firebase** for Crashlytics and Analytics

Works on Android 6.0+ (API 23), targets Android 16 (API 36).

---

## How the Code is Organized

```
com.doma.alsan/
├── AlsanApplication.kt   → Where I set up all the dependencies
├── data/                 → Everything related to data (APIs, storage, etc.)
├── helper/               → Utilities, enums, extensions
└── ui/                   → All the screens and UI stuff
```

---

## The Architecture

I use MVVM, which basically means:

```
Fragment → ViewModel → Repository → DataSource → API
```

**Fragments** observe the ViewModel's data using RxJava. When data changes, the UI updates automatically.

**ViewModels** handle the logic and expose reactive streams (`BehaviorSubject` for state, `PublishSubject` for events).

**Repositories** are the middlemen - they decide whether to fetch from the API or use cached data.

Every screen has its own ViewModel, all wired up through Koin in `AlsanApplication.kt`.

---

## Talking to AniList

AniList uses **GraphQL**, so I use **Apollo 3** as the client.

### How Authentication Works

1. User taps "Login"
2. App opens AniList's OAuth page in the browser
3. User logs in there
4. AniList redirects back with an access token
5. Token gets saved locally
6. Every API request now includes `Authorization: Bearer <token>`

### The GraphQL Files

All queries and mutations live in `app/src/main/graphql/`. Some important ones:

- `ViewerQuery` - Get the logged-in user's data
- `MediaQuery` - Get anime/manga details
- `MediaListCollectionQuery` - Get the user's lists
- `SaveMediaListEntryMutation` - Add or update a list entry
- `ToggleFavouritesMutation` - Favorite/unfavorite something

AniList has some custom types (`Json`, `CountryCode`) that are handled with custom adapters.

---

## Security Stuff

### Token Storage

Tokens aren't saved in plain SharedPreferences. Instead I use:

1. **Google Tink** for encryption (AES-256-GCM)
2. **Android Keystore** for key storage
3. **Jetpack DataStore** for the actual data

If decryption fails for some reason, the corrupted data gets wiped.

### Network

- HTTP logging is disabled in release builds
- Authorization header is never logged, even in debug
- Everything goes over HTTPS

---

## The Data Layer

There are several repositories:

- **UserRepository** - Login, profile, settings, notifications
- **MediaListRepository** - Anime/manga lists
- **ContentRepository** - Genres, tags, homepage stuff
- **BrowseRepository** - Search, media details, characters, staff, studios
- **SocialRepository** - Activities, likes, comments
- **InfoRepository** - External data from Jikan and AnimeThemes

Each repository talks to one or more DataSources that make the actual API calls.

---

## The UI

Feature modules, each with its own screens:

**Main stuff:** Home, Login, Splash, Landing
**Content:** Lists (with filter and reordering), Search, Seasonal charts, Calendar, Reviews & Reader, Characters, Staff, Studios
**Social:** Activity feed, Notifications, Following, Media Social
**Profile:** Stats (User & Media), Favorites
**Settings:** App settings, List customization, Filters, Account settings, AniList settings

### Media Details Structure
The media details page utilizes a **Tabbed Layout** to organize information efficiently:
- **Details**: General overview, genres, and synopsis.
- **Characters**: Grid of characters and their related voice actors.
- **Episodes**: Paginated list of anime episodes with external links support.
- **Staff**: List of production staff and their specific roles.
- **Recommendations**: Titles suggested based on similarity.

Every Fragment inherits from `BaseFragment`, which handles ViewBinding, RxJava disposal, and navigation helpers.

---

## Theming

Users can customize the app's appearance:

- **Themes**: Light and Dark modes.
- **Accents**: Yellow, Blue, Green, Pink, Purple.
- **Font Size**: Adjustable scale (Small, Normal, Large) applied globally.

---

## Other APIs

Besides AniList, the app also talks to:

- **Jikan** - MyAnimeList data
- **AnimeThemes** - OP/ED songs
- **YouTube** - Trailers/Episodes

These go through Retrofit.

---

*AL-san v3.2.0*
