## d90cc8c — 2025-05-08 (BillyMartinez)

Updated dependencies and refactored phone authentication UI.
Specifically, this commit:
- Updated the `firebaseCrashlyticsBuildtools` version in `libs.versions.toml`.
- Added `junitJupiter` dependency version and library definition in `libs.versions.toml`.
- Added `junit-jupiter` as a test implementation dependency in `app/build.gradle.kts`.
- Refactored the `OtpVerificationScreen` and the phone number input part of `RegisterScreenMobile.kt` to use a plain `String` for OTP value instead of `TextFieldValue`.
- Added `LocalSoftwareKeyboardController` to hide the keyboard when actions are triggered in `RegisterScreenMobile.kt`.
- Ensured OTP input is limited to 6 digits and only accepts digit characters.
- Added a `Box(modifier = Modifier.size(0.dp))` to make the underlying `BasicTextField` in `OtpVerificationScreen` invisible while still allowing input.
- Updated the "Verificar" button enabled state to rely on the `String` length instead of `TextFieldValue.text.length`.
- Added a KDoc comment to `OtpVerificationScreen`.
- Replaced `Icons.AutoMirrored.Filled.ArrowBack` with `Icons.Default.ArrowBack` in `OtpVerificationScreen`.


## b0624f8 — 2025-05-07 (BillyMartinez)

Updated dependencies and refactored phone authentication UI.
Specifically, this commit:
- Updated the `firebaseCrashlyticsBuildtools` version in `libs.versions.toml`.
- Added `junitJupiter` dependency version and library definition in `libs.versions.toml`.
- Added `junit-jupiter` as a test implementation dependency in `app/build.gradle.kts`.
- Refactored the `OtpVerificationScreen` and the phone number input part of `RegisterScreenMobile.kt` to use a plain `String` for OTP value instead of `TextFieldValue`.
- Added `LocalSoftwareKeyboardController` to hide the keyboard when actions are triggered in `RegisterScreenMobile.kt`.
- Ensured OTP input is limited to 6 digits and only accepts digit characters.
- Added a `Box(modifier = Modifier.size(0.dp))` to make the underlying `BasicTextField` in `OtpVerificationScreen` invisible while still allowing input.
- Updated the "Verificar" button enabled state to rely on the `String` length instead of `TextFieldValue.text.length`.
- Added a KDoc comment to `OtpVerificationScreen`.
- Replaced `Icons.AutoMirrored.Filled.ArrowBack` with `Icons.Default.ArrowBack` in `OtpVerificationScreen`.


## 5349bf6 — 2025-05-07 (BillyMartinez)

Updated dependencies and refactored phone authentication UI.
Specifically, this commit:
- Updated the `firebaseCrashlyticsBuildtools` version in `libs.versions.toml`.
- Added `junitJupiter` dependency version and library definition in `libs.versions.toml`.
- Added `junit-jupiter` as a test implementation dependency in `app/build.gradle.kts`.
- Refactored the `OtpVerificationScreen` and the phone number input part of `RegisterScreenMobile.kt` to use a plain `String` for OTP value instead of `TextFieldValue`.
- Added `LocalSoftwareKeyboardController` to hide the keyboard when actions are triggered in `RegisterScreenMobile.kt`.
- Ensured OTP input is limited to 6 digits and only accepts digit characters.
- Added a `Box(modifier = Modifier.size(0.dp))` to make the underlying `BasicTextField` in `OtpVerificationScreen` invisible while still allowing input.
- Updated the "Verificar" button enabled state to rely on the `String` length instead of `TextFieldValue.text.length`.
- Added a KDoc comment to `OtpVerificationScreen`.
- Replaced `Icons.AutoMirrored.Filled.ArrowBack` with `Icons.Default.ArrowBack` in `OtpVerificationScreen`.


## 0871d30 — 2025-05-07 (BillyMartinez)

Feat: Added profile editing and location features.
Specifically, this commit:
- Updated `AuthManager.UserProfile` data class to include latitude and longitude.
- Modified `AuthManager` methods (`isProfileComplete`, `getUserProfile`, `updateUserProfile`) to handle latitude and longitude.
- Added a new function `getAddressFromLocation` to `AuthManager` to get an address string from coordinates using Geocoder.
- Added profile editing functionality to `SettingsScreen` via a new `ProfileEditDialog`.
- The `ProfileEditDialog` allows users to update their name, age (using a slider), profile photo (via photo picker), location name, and coordinates.
- Integrated location detection (`FusedLocationProviderClient`) and Geocoder in `ProfileEditDialog` to allow users to set their current location and automatically populate the address field.
- Added age, location, and coordinates display to the user profile section in `SettingsScreen`.
- Made minor adjustments to the `ProfileCompletionScreen` to use the updated `updateUserProfile` method signature (though latitude/longitude are set to null initially there).


## 5591f70 — 2025-05-07 (BillyMartinez)

Feat: Added profile editing and location features.
Specifically, this commit:
- Updated `AuthManager.UserProfile` data class to include latitude and longitude.
- Modified `AuthManager` methods (`isProfileComplete`, `getUserProfile`, `updateUserProfile`) to handle latitude and longitude.
- Added a new function `getAddressFromLocation` to `AuthManager` to get an address string from coordinates using Geocoder.
- Added profile editing functionality to `SettingsScreen` via a new `ProfileEditDialog`.
- The `ProfileEditDialog` allows users to update their name, age (using a slider), profile photo (via photo picker), location name, and coordinates.
- Integrated location detection (`FusedLocationProviderClient`) and Geocoder in `ProfileEditDialog` to allow users to set their current location and automatically populate the address field.
- Added age, location, and coordinates display to the user profile section in `SettingsScreen`.
- Made minor adjustments to the `ProfileCompletionScreen` to use the updated `updateUserProfile` method signature (though latitude/longitude are set to null initially there).


## 9461e6b — 2025-05-07 (BillyMartinez)

Updated project dependencies and configurations.
Specifically, this commit:
- Updated AGP to 8.10.0 in `gradle/libs.versions.toml`.
- Added Firebase Crashlytics dependency and plugin definition in `gradle/libs.versions.toml`.
- Applied the Firebase Crashlytics plugin in the app-level `build.gradle.kts`.
- Added the Firebase Crashlytics plugin definition to the top-level `build.gradle.kts`.
- Removed the XML declaration from `.idea/misc.xml`.
- Removed a specific run configuration selection state from `.idea/deploymentTargetSelector.xml`.


## de56bad — 2025-05-07 (BillyMartinez)

Updated project dependencies and configurations.
Specifically, this commit:
- Updated AGP to 8.10.0 in `gradle/libs.versions.toml`.
- Added Firebase Crashlytics dependency and plugin definition in `gradle/libs.versions.toml`.
- Applied the Firebase Crashlytics plugin in the app-level `build.gradle.kts`.
- Added the Firebase Crashlytics plugin definition to the top-level `build.gradle.kts`.
- Removed the XML declaration from `.idea/misc.xml`.
- Removed a specific run configuration selection state from `.idea/deploymentTargetSelector.xml`.


## a9d72c1 — 2025-05-07 (BillyMartinez)

Updated project dependencies and configurations.
Specifically, this commit:
- Updated AGP to 8.10.0 in `gradle/libs.versions.toml`.
- Added Firebase Crashlytics dependency and plugin definition in `gradle/libs.versions.toml`.
- Applied the Firebase Crashlytics plugin in the app-level `build.gradle.kts`.
- Added the Firebase Crashlytics plugin definition to the top-level `build.gradle.kts`.
- Removed the XML declaration from `.idea/misc.xml`.
- Removed a specific run configuration selection state from `.idea/deploymentTargetSelector.xml`.


## 69dfc98 — 2025-05-07 (BillyMartinez)

Updated project dependencies and configurations.
Specifically, this commit:
- Updated AGP to 8.10.0 in `gradle/libs.versions.toml`.
- Added Firebase Crashlytics dependency and plugin definition in `gradle/libs.versions.toml`.
- Applied the Firebase Crashlytics plugin in the app-level `build.gradle.kts`.
- Added the Firebase Crashlytics plugin definition to the top-level `build.gradle.kts`.
- Removed the XML declaration from `.idea/misc.xml`.
- Removed a specific run configuration selection state from `.idea/deploymentTargetSelector.xml`.


## 8848a68 — 2025-05-06 (Billy)

Updated application version and minor code cleanup.
Specifically, this commit:
- Updated the application version code to 2 and the version name to "1.1" in `app/build.gradle.kts`.
- Removed unused `unspecified_scheme` variable from `app/src/main/java/com/vecinapp/ui/theme/Theme.kt`.
- Changed `LoginStep.Choice` and `LoginStep.PhoneInput` to use `data object` instead of `object` in `app/src/main/java/com/vecinapp/ui/screen/LoginScreen.kt`.
- Removed unnecessary `@OptIn(ExperimentalMaterial3Api::class)` annotations from `LoginScreen.kt` and `SettingsScreen.kt`.
- Removed unused `infiniteColorTransition` function from `app/src/main/java/com/vecinapp/presentation/ColorTransitions.kt`.
- Removed unused `btnH` property from the `Dim` object in `app/src/main/java/com/vecinapp/ui/VecinalUi.kt`.


## 99db115 — 2025-05-06 (Billy)

Refactor: Improve Senior Mode UI and icons.
Specifically, this commit:
- Simplified the Top App Bar for Senior Mode in `SettingsScreen.kt` by removing the TopAppBar composable and directly using an IconButton for navigation.
- Changed the "Send" icon in `RegisterScreenMobile.kt` to use `Icons.AutoMirrored.Filled.Send` for potentially better LTR/RTL compatibility.


## c748c1f — 2025-05-06 (Billy)

Refactor: Adjusted bottom navigation bar layout and FAB position.
Specifically, this commit:
- Modified the dimensions and padding of the `BottomNavigationBar` composable. The height was increased from 85.dp to 132.dp, and the padding was changed to align content to the top center.
- Swapped the positions of the "Sugerencias" and "Eventos" navigation items in the bottom bar.
- Adjusted the vertical offset of the Community Floating Action Button (FAB) from -20.dp to -30.dp to position it higher.
- Ensured haptic feedback is performed on item click in `NavBarItem` and `ProfileNavItem`.


## c17b71e — 2025-05-06 (Billy)

Revamped bottom navigation bar UI and logic.
Specifically, this commit:
- Updated the app icon displayed in the `MainActivity` top app bar, adding padding around it.
- Modified `VecinalNavHost.kt` to define navigation routes using sealed interfaces and data classes for improved type safety and clarity.
- Reworked the `BottomNavigationBar` composable in `BottomNavigationBar.kt`:
  - Introduced a new rounded shape and subtle shadow for the background surface, following Material Design 2 guidelines for bottom navigation [1].
  - Redesigned the navigation items to include a rounded background indicator on selection and a colored line below the text.
  - Added animations for scaling and indicator height when a navigation item is selected.
  - Created dedicated `NavBarItem` and `ProfileNavItem` composables to handle the styling and logic for standard and profile navigation items, respectively.
  - Integrated haptic feedback for navigation item clicks.
  - Modified the Community FAB's positioning and added a simple scaling animation on initial display.
  - Updated the navigation logic within the items to correctly navigate and manage the back stack.


## 4759d65 — 2025-05-06 (Billy)

Feat: Implement settings screen with profile info and appearance options.
Specifically, this commit:
- Added a `SettingsScreen` composable with sections for Appearance, Visual Mode, and Account, based on Android development guidelines for settings (Reference [1]).
- Included options for Dark Mode and Dynamic Color (Monet) in the Appearance section, controlled by `Switch` components.
- Implemented a Visual Mode section with buttons to switch between "Senior" and "Normal" modes, with a descriptive card for the Senior mode.
- Added an Account section displaying the user's linked phone number and a button to link/update it.
- Included a "Logout" button to sign the user out.
- Integrated user profile information (name, email, photo, creation/last sign-in time) into the Settings screen header, with an edit profile button placeholder.
- Created helper composables `SettingsSection` and `SettingsRow` for better structure and reusability of the settings list items.
- Added basic iconography and styling for the settings elements.
- Updated `MainActivity` to conditionally show the `BottomNavigationBar` only when a user is logged in.


## 6609687 — 2025-05-06 (Billy)

Feat: Implement settings screen with profile info and appearance options.
Specifically, this commit:
- Added a `SettingsScreen` composable with sections for Appearance, Visual Mode, and Account, based on Android development guidelines for settings (Reference [1]).
- Included options for Dark Mode and Dynamic Color (Monet) in the Appearance section, controlled by `Switch` components.
- Implemented a Visual Mode section with buttons to switch between "Senior" and "Normal" modes, with a descriptive card for the Senior mode.
- Added an Account section displaying the user's linked phone number and a button to link/update it.
- Included a "Logout" button to sign the user out.
- Integrated user profile information (name, email, photo, creation/last sign-in time) into the Settings screen header, with an edit profile button placeholder.
- Created helper composables `SettingsSection` and `SettingsRow` for better structure and reusability of the settings list items.
- Added basic iconography and styling for the settings elements.
- Updated `MainActivity` to conditionally show the `BottomNavigationBar` only when a user is logged in.


## da11b2c — 2025-05-06 (Billy)

Enhanced phone number registration screens with improved UI and error handling.
Specifically, this commit:
- Removed the `fillMaxSize()` modifier from the `VecinalNavHost` in `MainActivity.kt` as it's applied internally within the `NavHost`.
- Removed the `fillMaxSize()` modifier parameter from the `VecinalNavHost` composable and applied it directly to the `NavHost` internally.
- Refactored `RegisterScreenMobile` and `RegisterScreenOtp` composables to improve the UI and user experience.
- Added modern Material Design 3 components (Card, Surface, updated Buttons and TextFields) and improved layout using padding, spacing, and alignment.
- Added visual feedback for the verification process with icons, background shapes, and informative text.
- Integrated loading states and error message display with `isLoading` and `errorMessage` states and animated visibility.
- Added focus requesters to automatically focus the input fields on screen entry.
- Improved error handling for Firebase phone authentication callbacks and credential verification, providing more informative error messages to the user.
- Removed basic `Toast` messages and replaced them with integrated UI error displays.
- Added a "Reenviar código" TextButton with an icon in `RegisterScreenOtp`.


## ea8490a — 2025-05-06 (Billy)

feat: Configured initial navigation flow based on user authentication state.
Specifically, this commit:
- Refactored `MainActivity.kt` to handle initial navigation logic based on whether a user is logged in (`user == null`) and if it's their first time using the app (`isFirstTime`).
- Removed direct calls to `LoginScreen` and `OnboardingModeScreen` from `MainActivity`.
- Passed the `user` state from `MainActivity` to `VecinalNavHost` to manage navigation based on authentication.
- Added a `ScreenLogin` serializable route to `VecinalNavHost.kt`.
- Modified `VecinalNavHost` to set its `startDestination` dynamically based on the `user` and `isFirstTime` states, navigating to login, onboarding, or dashboard accordingly.
- Integrated the `LoginScreen` and `OnboardingModeScreen` into the `VecinalNavHost` composable, defining their routes and navigation logic within the graph.
- Ensured that after successful login or completing onboarding, the navigation stack is cleared using `popUpTo` to prevent returning to previous screens using the back button.
- Added the user state to `BottomNavigationBar` in `MainActivity` to conditionally show the bar.


## a8efdd8 — 2025-05-06 (Billy)

Refactor: Improved PreferencesManager and MainActivity logic.
Specifically, this commit:
- Simplified the logic in `MainActivity.kt` for handling user authentication state, navigation, and data store preferences. The `when` statement for different app states (login, onboarding, main) was replaced with more direct conditional checks.
- The `onLoggedOut` lambda in `VecinalNavHost` now handles signing out the user and navigating back to the onboarding screen after logout.
- Added a log statement in `MainActivity.kt` to show the `isFirstTime` value for debugging.
- In `PreferencesManager.kt`, the default value for `dynamicColor` is now set based on the Android SDK version (`Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`), enabling dynamic colors by default on supported devices.


## f4d9eb4 — 2025-05-06 (Billy)

Updated onboarding flow and preferences management.
Specifically, this commit:
- Made preferences (dark mode, dynamic color, senior mode, first time flag) persistent using Jetpack DataStore.
- Read initial preferences synchronously and then collect updates asynchronously in `MainActivity`.
- Updated the `MainActivity` to conditionally show the login screen, the new onboarding mode selection screen, or the main navigation host based on the authentication state and the `isFirstTime` preference.
- Modified the `OnboardingModeScreen` to take suspend functions for updating preferences and a lambda for navigation, reflecting the DataStore interaction.
- Updated the `VecinalNavHost` signature to include the `onFirstTimeChange` callback and adjusted the start destination logic.
- Ensured that after login, the app navigates to the onboarding screen if it's the first time, otherwise to the dashboard.
- Modified the logout process to reset the `isFirstTime` preference and sign out the Firebase user.
- Added modifier padding to `VecinalNavHost` to respect the Scaffold inner padding.
- Added a size and padding to the app icon in the `MainActivity` TopAppBar.


## 35a838b — 2025-05-06 (Billy)

Refactor: Improved Onboarding Mode Screen Layout.
Specifically, this commit:
- Modified the layout of the `OnboardingModeScreen` to display the "Standard" and "Senior" mode selection cards side-by-side horizontally.
- Reduced the height of the `ModeCard` composable from 160.dp to 140.dp.
- Reduced the internal padding of the `ModeCard` from 16.dp to 12.dp.
- Reduced the size of the icon within the `ModeCard` from 48.dp to 40.dp.
- Reduced the vertical spacing after the icon in `ModeCard` from 16.dp to 12.dp.
- Changed the title text style in `ModeCard` from `headlineSmall` to `titleLarge`.
- Adjusted various vertical paddings (`Spacer` heights) throughout the `OnboardingModeScreen` for better spacing.
- Added a `verticalScroll` modifier to the main `Column` in `OnboardingModeScreen` to allow scrolling if the content exceeds screen height.
- Added a conditional `Card` that is only displayed when the "Senior" mode is selected. This card contains the list of features offered by the Senior mode, presented using the `SeniorFeatureItem` composable.
- Added an additional spacer at the bottom of the screen after the "Continuar" button.


## 49baaca — 2025-05-06 (Billy)

Refactor: Redesigned Onboarding mode selection screen.
Specifically, this commit:
- Replaced the previous `ModeOptionCard` composable with a new `ModeCard` composable that uses a different card design (simple Card instead of ElevatedCard) and layout.
- Modified the layout of the mode selection screen (`OnboardingModeScreen`) to be a `Column` starting at the top instead of centering the content in a `Box`.
- Added header text "Hola! Bienvenido!" to the onboarding screen.
- Added descriptive text and a list of features for the Senior mode using a new `SeniorFeatureItem` composable.
- Updated the "Continuar" button style and position to be at the bottom of the screen and span the full width.
- Removed unused imports and commented-out code.


## 0aa58f7 — 2025-05-06 (Billy)

Redesigned onboarding mode selection screen.
Specifically, this commit:
- Replaced the basic `ModeSwitchButton` with a more visually appealing `ModeOptionCard` composable.
- Updated the `OnboardingModeScreen` layout to use `ModeOptionCard` for selecting between "Estándar" and "Senior" modes.
- Added animations (elevation, scale, fadeIn/fadeOut) to the `ModeOptionCard` for visual feedback on selection.
- Added a header illustration and descriptive text to the `OnboardingModeScreen`.
- Refactored the layout of the `OnboardingModeScreen` to use `Box` and `Column` for better structure and centering.
- Added padding and spacing for improved visual design.
- Updated the continue button styling and added an arrow icon.
- Removed the now unused `ModeSwitchButton` and `CenterText` composables.
- Added a `@Preview` to the `OnboardingModeScreen` for easier development.
- Simplified the content of placeholder screens (`AnunciosScreen`, `PanelDirectivoScreen`, `SugerenciasListScreen`, `TablonListScreen`) to just display a `Text` composable.


## 9063e0f — 2025-05-06 (Billy)

Initial Onboarding flow.
Specifically, this commit:
- Added an `isFirstTime` preference to `PreferencesManager` and `UserPreferences` data class.
- Modified `MainActivity` to observe the `isFirstTime` preference and control the visibility of the bottom navigation bar based on this value and `seniorMode`.
- Updated `VecinalNavHost` to use `ScreenOnboarding` as the starting destination only if `isFirstTime` is true, otherwise starting at `ScreenDashboard`.
- Moved `ModeSwitchButton` and `CenterText` composables from `VecinalUi.kt` to `OnboardingModeScreen.kt` and `AnunciosScreen.kt`, `PanelDirectivo.kt`, `SugerenciasListScreen.kt`, and `TablonListScreen.kt` respectively.
- Added a "Continuar" button with a community icon to `OnboardingModeScreen.kt`.
- Adjusted the background gradient in `OnboardingModeScreen.kt`.
- Modified the layout of the bottom navigation bar in `BottomNavigationBar.kt` to improve spacing.


## 9387e6b — 2025-05-06 (Billy)

Refactor: Update Bottom Navigation Bar and Event List Screen.
Specifically, this commit:
- Updated the `.idea/misc.xml` file, setting the `default` attribute to `true` for the project JDK configuration.
- Modified the `EventosListScreen.kt` to:
    - Remove the `Scaffold` and `CenterAlignedTopAppBar`.
    - Add a `Column` as the root layout with padding.
    - Add a "Próximos eventos" text label with `titleLarge` typography.
    - Add horizontal and vertical padding to the `LazyColumn`.
- Modified `BottomNavigationBar.kt` to:
    - Change the icons and text labels for the navigation items to reflect "Sugerencias", "Eventos", and "Inicio" instead of the previous labels ("Inicio", "Historial", "Recompensas").
    - Update the navigation targets of the buttons to `ScreenAnuncios`, `ScreenEventos`, and `ScreenSugerencias`.
    - Change the FloatingActionButton's icon to `Icons.Filled.Diversity3` and its click action to navigate to `ScreenEventos`.


## 8baae6b — 2025-05-06 (Billy)

Refactor: Improved navigation and settings screen implementation.
Specifically, this commit:
- Updated `MainActivity` to handle navigation using `rememberNavController`.
- Implemented logic in `MainActivity` to show/hide top and bottom bars based on the current route and senior mode status.
- Wrapped the `VecinalNavHost` in `MainActivity` within a `Box` with padding.
- Updated `VecinalNavHost` to use `fillMaxSize()` as the default modifier and removed the padding modifier from the call in `MainActivity`.
- Corrected the call to `VecinalNavHost` in `MainActivity` to pass the `darkMode` and `dynamicColors` parameters.
- Defined a helper function `toRoute()` for `ScreenSettings` to get its serializable route string.
- Updated the `SettingsScreen` composable:
    - Added a `TopAppBar` for navigation back when in senior mode.
    - Ensured consistent spacing between UI elements.
- Added a preview composable `DefaultPreview` to `MainActivity`.
- Updated the `.idea/deploymentTargetSelector.xml` file to include deployment targets for "app" and "SettingsScreenPreview".


## a3d3434 — 2025-05-05 (Billy)

Refactor: Code cleanup and minor UI adjustments.
Specifically, this commit:
- Added the `ContrastLevel` enum to `Theme.kt`.
- Updated the `EventCard` composable in `EventosListScreen.kt` to use named arguments for better readability.
- Refactored the `OnboardingModeScreen.kt` to improve the button layout and shape consistency.
- Improved error handling and logging in `RegisterScreenMobile.kt` when phone authentication fails.
- Removed unnecessary blank lines in `BottomNavigationBar.kt`.
- Adjusted button colors and borders in `GoogleSignInButton.kt` for different themes and added comments regarding icon size.
- Updated `MainActivity.kt` to ensure proper data flow from DataStore to UI state and fixed the padding and size modifiers for the app icon in the top app bar. Also ensured that dynamic colors and dark mode states from preferences are passed to `VecinalNavHost`.
- Made minor structural adjustments in `LoginScreen.kt`'s sealed class definition.
- Modified `VecinalNavHost.kt` to pass the dynamic colors and dark mode preference states down to the `SettingsScreen`. Also made minor formatting adjustments to the route definitions.
- Removed unused imports in `SettingsScreen.kt` and added `ArrowBack` and `Logout` icons from `Icons.AutoMirrored.Filled`.
- Replaced `Divider()` with `HorizontalDivider()` in `EventDetailScreen.kt` and updated icon usage to `Icons.AutoMirrored.Filled.ArrowBack`.
- Updated the `Tablón` icon in `DashboardScreen.kt` from `Icons.Default.Chat` to `Icons.AutoMirrored.Filled.Chat` and made minor formatting adjustments.
- Adjusted the scaling factor logic and removed unused import in `VecinalUi.kt`.
- Made minor formatting adjustments in `TablonListScreen.kt`, `PanelDirectivo.kt`, and `AnunciosScreen.kt`.


## 2f9463b — 2025-05-05 (Billy)

feat: Implement phone number login and profile completion
Specifically, this commit:
- Removed previous placeholder data models (`Announcement.kt`, `BoardPost.kt`, `Community.kt`, `Event.kt`, `Meeting.kt`, `Resource.kt`, `Suggestion.kt`, `User.kt`).
- Introduced new data models in `Model.kt` for `Role`, `Status`, `RsvpStatus`, `NotificationSubscription`, `Membership`, `MembershipRequest`, `DirectiveMember`, `Comment`, `Attachment`, `Rsvp`, `Vote`, `Report`, `Announcement`, `Event`, `Proposal`, and `Community`. These new models use `@Serializable` and include Firebase-specific annotations (`@ServerTimestamp`, `@Contextual` for `Timestamp` and `GeoPoint`).
- Updated the `LoginScreen.kt` to include a choice between Google, SMS, and Guest login.
- Added `LoginStep` sealed class to manage the login flow state (Choice, PhoneInput, Otp).
- Implemented SMS login flow using Firebase Phone Auth in `RegisterScreenMobile.kt`, including sending the code and verifying the OTP.
- Added a placeholder `ProfileCompletionScreen.kt` for users to complete their profile after phone verification.
- Updated `VecinalNavHost.kt` to include new navigation routes for `ScreenRegisterPhone` and `ScreenProfileCompletion`.
- Modified the navigation flow in `VecinalNavHost.kt` to direct users from phone registration/OTP verification to profile completion, and then to the dashboard.
- Added a link/update phone number option in the `SettingsScreen.kt` that navigates to the phone registration flow.


## d9e0903 — 2025-05-05 (Billy)

feat: Implement phone number login and profile completion
Specifically, this commit:
- Removed previous placeholder data models (`Announcement.kt`, `BoardPost.kt`, `Community.kt`, `Event.kt`, `Meeting.kt`, `Resource.kt`, `Suggestion.kt`, `User.kt`).
- Introduced new data models in `Model.kt` for `Role`, `Status`, `RsvpStatus`, `NotificationSubscription`, `Membership`, `MembershipRequest`, `DirectiveMember`, `Comment`, `Attachment`, `Rsvp`, `Vote`, `Report`, `Announcement`, `Event`, `Proposal`, and `Community`. These new models use `@Serializable` and include Firebase-specific annotations (`@ServerTimestamp`, `@Contextual` for `Timestamp` and `GeoPoint`).
- Updated the `LoginScreen.kt` to include a choice between Google, SMS, and Guest login.
- Added `LoginStep` sealed class to manage the login flow state (Choice, PhoneInput, Otp).
- Implemented SMS login flow using Firebase Phone Auth in `RegisterScreenMobile.kt`, including sending the code and verifying the OTP.
- Added a placeholder `ProfileCompletionScreen.kt` for users to complete their profile after phone verification.
- Updated `VecinalNavHost.kt` to include new navigation routes for `ScreenRegisterPhone` and `ScreenProfileCompletion`.
- Modified the navigation flow in `VecinalNavHost.kt` to direct users from phone registration/OTP verification to profile completion, and then to the dashboard.
- Added a link/update phone number option in the `SettingsScreen.kt` that navigates to the phone registration flow.


## 3106932 — 2025-05-05 (Billy)

Refactor: Moved screens into dedicated screen package.
Specifically, this commit:
- Moved several screen composables (AnunciosScreen, DashboardScreen, EventosListScreen, OnboardingModeScreen, PanelDirectivoScreen, SugerenciasListScreen, TablonListScreen) from `VecinalUi.kt` into a new package `com.vecinapp.ui.screen`.
- Updated imports in `VecinalNavHost.kt` and `VecinalUi.kt` to reflect the new screen locations.
- Made `Dim`, `ModeSwitchButton`, and `CenterText` objects/composables internal in `VecinalUi.kt` as they are intended for internal use within the UI module.
- Added new files for each moved screen composable in the `com.vecinapp.ui.screen` directory.


## 36f5cd6 — 2025-05-05 (Billy)

Improved Git ignore configuration.
Specifically, this commit:
- Added new entries to `.gitignore` to ignore `.idea/appInsightsSettings.xml` and a specific `.kotlin/errors` log file (`errors-1746455924526.log`). This prevents IDE-specific settings and temporary build-related files from being tracked by Git.


## 112758a — 2025-05-05 (Billy)

Cleaned up IDE configuration files.
Specifically, this commit:
- Removed unnecessary XML declaration from `.idea/misc.xml`.
- Added `GradleMigrationSettings` to `.idea/gradle.xml`.
- Removed a specific run configuration selection state from `.idea/deploymentTargetSelector.xml`.


## 1eeb857 — 2025-05-05 (BillyMartinez)

Feat: Added new logo with text and guest login button.
Specifically, this commit:
- Added a new logo asset (`Icon_text.svg`) that includes the app name text.
- Created an XML drawable resource (`app/src/main/res/drawable/icon_text.xml`) for the new logo.
- Updated the login screen (`LoginScreen.kt`) to use the new logo.
- Added a "Login as Guest" button to the login screen.
- Added a log message to the `BottomNavigationBar.kt` to show user information when navigating to the profile screen.


## 7ad39a9 — 2025-05-05 (BillyMartinez)

new


## 0fed042 — 2025-05-05 (BillyMartinez)

Updated app icons and colors.
Specifically, this commit:
- Added new app icon assets (`icon_only.svg`, `app/src/main/res/drawable/icon_only.xml`, `app/src/main/res/drawable/asdasd.xml`).
- Configured adaptive icons for launcher (`app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`, `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`) using the new assets.
- Defined a background color for the launcher icons in `app/src/main/res/values/ic_launcher_background.xml`.
- Replaced the default Compose theme colors with a new set of Material Design 3 colors in `app/src/main/java/com/vecinapp/ui/theme/Color.kt`. This includes light, dark, medium contrast, and high contrast color schemes.
- Updated the `VecinappTheme` composable in `app/src/main/java/com/vecinapp/ui/theme/Theme.kt` to use the new color schemes and added logic for handling dynamic colors and contrast levels (though dynamic color is currently disabled).
- Integrated the new `icon_only` drawable as the navigation icon in the `MainActivity` top app bar.
- Added a new Google Play Store icon asset (`app/src/main/ic_launcher-playstore.png`).


## c714f3a — 2025-05-05 (BillyMartinez)

Configured base navigation structure with a senior mode.
Specifically, this commit:
- Configured type-safe navigation using `@Serializable` routes for screens like Onboarding, Dashboard, Announcements, Events, Suggestions, Board, and Panel.
- Created placeholder screens for Announcements, Suggestions, Board, and Panel.
- Implemented a detailed `EventDetailScreen` composable with placeholder data and UI elements for event details, organizer information, and location (with a map placeholder).
- Defined a `VecinalNavHost` composable to manage the app's navigation using `NavHost` and the defined serializable routes.
- Introduced a senior mode scaling factor (`Dimens.scale`) managed by the `setSeniorMode` function in `VecinalUi.kt`.
- Created common button composables (`FilledBtn` and `OutBtn`) with senior mode scaling applied.
- Designed a `DashboardSeniorScreen` with a grid of card buttons for main features, incorporating the senior mode scaling and navigation.
- Added `EventCard` composable for displaying event information in a list.
- Updated `MainActivity` to use `VecinalTheme`, `VecinalNavHost`, and potentially handle user authentication state with Firebase Auth (although commented out).
- Integrated Firebase Auth, Firestore, Maps Compose, Maps KTX, Coil Compose, and Kotlinx Serialization dependencies.
- Configured Gradle plugins for Google Services and Maps Platform Secrets.
- Defined data models for `Announcement`, `BoardPost`, `Community`, `Event`, `Meeting`, `Suggestion`, and `User` in the `domain.model` package.
- Added a `PreferencesManager` class using Jetpack DataStore for user preferences (dark mode and dynamic color), although not fully integrated into the UI in this commit.
- Updated the app icon assets and defined a background color for the adaptive icon.
- Added a new `asdasd.svg` file (likely a new icon asset).


## 39a8d94 — 2025-05-05 (BillyMartinez)

Updated project dependencies and build configurations.
Specifically, this commit:
- Updated AGP to 8.9.2 and Kotlin to 2.0.21.
- Updated several AndroidX and Compose library versions in `libs.versions.toml`.
- Removed deprecated and unused dependency definitions from `libs.versions.toml` (Firebase, Hilt, Maps, Coil, some Compose).
- Updated string resource formatting in `app/src/main/res/values/strings.xml`.
- Removed vector drawables support library usage from `app/build.gradle.kts`.
- Removed several unused plugins from `app/build.gradle.kts`.
- Removed the MAPS_API_KEY metadata from `app/src/main/AndroidManifest.xml`.
- Added a new VCS configuration file `.idea/vcs.xml`.
- Added a new google-services.json file.
- Updated the top-level build.gradle.kts file to reflect plugin changes.


## f868542 — 2025-05-05 (Billy Martinez)

Initial commit

