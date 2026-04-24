# Call Limiter

Call Limiter is an Android application designed to help users set a time limit for phone calls to specific contacts. This app ensures calls do not exceed the defined duration, making it easier to manage call times effectively.

<div align="center">

[<img src="https://f-droid.org/badge/get-it-on.png"
    alt="Get it on F-Droid"
    height="80">](https://f-droid.org/packages/com.thirumalai.calllimiter)
[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroidButtonGreyBorder_nofont.png" 
    height="80" 
    alt="Get it at IzzyOnDroid">](https://apt.izzysoft.de/packages/com.thirumalai.calllimiter)
[<img src="https://s1.ax1x.com/2023/01/12/pSu1a36.png" alt="Download from GitHub" height="75">](https://github.com/Thiru-Malai/CallLimiter/releases)

</div>


<div align="center">

![F-Droid Version](https://img.shields.io/f-droid/v/com.thirumalai.calllimiter?style=for-the-badge&color=blue)
![IzzyOnDroid](https://img.shields.io/endpoint?url=https://apt.izzysoft.de/fdroid/api/v1/shield/com.thirumalai.calllimiter&label=IzzyOnDroid&style=for-the-badge)
![GitHub Release](https://img.shields.io/github/v/release/Thiru-Malai/Calllimiter?style=for-the-badge&label=GITHUB&color=blue)

![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/Thiru-Malai/CallLimiter/total?style=for-the-badge&label=GITHUB%20DOWNLOADS&color=green)
[![IzzyOnDroid Monthly Downloads](https://img.shields.io/badge/dynamic/json?url=https://dlstats.izzyondroid.org/iod-stats-collector/stats/basic/monthly/rolling.json&query=$.['com.thirumalai.calllimiter']&label=IzzyOnDroid%20monthly%20downloads&style=for-the-badge&color=green)](https://apt.izzysoft.de/packages/com.thirumalai.calllimiter)
[![IzzyOnDroid Yearly Downloads](https://img.shields.io/badge/dynamic/json?url=https://dlstats.izzyondroid.org/iod-stats-collector/stats/basic/yearly/rolling.json&query=$.['com.thirumalai.calllimiter']&label=IzzyOnDroid%20yearly%20downloads&style=for-the-badge&color=green)](https://apt.izzysoft.de/packages/com.thirumalai.calllimiter)

<a href="https://buymeacoffee.com/thirumalaikg">
    <img width="218" height="61" alt="buymeacoffee" src="https://github.com/user-attachments/assets/6149df1a-9555-4293-a8e5-1448e013201b" />
</a>
</div>

<br>

<div align="center">
    <img src="./metadata/en-US/images/featureGraphic.png" alt="Feature Graphic" />
</div>

## 🌟Features
<div align="center">
  <img src="metadata/en-US/images/phoneScreenshots/1.png" width="23%" alt="Onboarding Page"/>
  <img src="metadata/en-US/images/phoneScreenshots/2.png" width="23%" alt="Home Screen"/>
  <img src="metadata/en-US/images/phoneScreenshots/3.png" width="23%" alt="Add Limits to Contacts"/>
  <img src="metadata/en-US/images/phoneScreenshots/4.png" width="23%" alt="Settings"/>
</div>

<br></br>

- 🔢 **Set time limits** for specific phone numbers
- 📴 **Auto-disconnect calls** when the limit is reached
- 🎡 **Bottom sheet wheel selector** for choosing time duration
- 🌐 **Global Call Limit** apply default time limit to all contacts automatically
- 📂 **Persistent storage** – limits remain saved until deleted
- 🗑️ **Delete or edit limits** for specific numbers anytime
- 🎨 **Dark & Light themes** with system theme support
- ⚡ **Emergency Buffer Time** (extra seconds for critical calls)
- 🔒 **Privacy-first** – works fully offline, no data is ever shared

## ⚒ How It Works

1. **Enter a Phone Number**: Manually enter a number or select a contact, with the option to add or edit the contact name.
2. **Set a Time Limit**: Choose a duration using the bottom sheet timer.
3. **Save the Limit**: The app stores the number and its corresponding time limit.
4. **Monitor Calls**: Calls to the saved number will be restricted based on the set time.
5. **Delete a Limit**: Users can remove the time restriction for a number anytime.

This app is ideal for managing call durations effectively, whether for personal use or controlling excessive call times.

## 🔐 Permissions Used

Call Limiter requires the following Android permissions:

- **READ_PHONE_STATE** → Detect ongoing calls
- **READ_CALL_LOG** → Identify call history for managing limits
- **CALL_PHONE** → Disconnect calls when limit is reached
- **ANSWER_PHONE_CALLS** → End calls programmatically
- **POST_NOTIFICATIONS** → Show reminders and call limit alerts
- **FOREGROUND_SERVICE** → Run safely in the background

> ✅ These permissions are **only used to enforce your call limits**.  
> ✅ The app works **fully offline**.  
> ✅ **Your privacy is our top priority** – no personal data is ever collected or shared.

## ⚙️ Settings

- 🌓 **Theme Selection** → Choose System / Light / Dark mode
- ⏳ **Emergency Buffer Time** → Add 10s – 5min extra if needed
- ⏳ **Call Start Buffer Time** → Enable or disable a 10-second buffer at the beginning of each call
- 🌐 **Global Call Limit** → Apply default time limit to all contacts automatically
- ⏳ **Limit Scope (Per Call / Per Day)** → When enabled, the limit resets after each call. Otherwise, it resets daily 
- 📜 **Permissions Page** → View and manage required app permissions
- 🆘 **Support / Help** → Redirect to GitHub Issues for reporting bugs
- ℹ️ **About** → Author, Repository, Change Log, Terms & Conditions and Privacy Policy

## 🤝 Contributing

Want to make **Call Limiter** even better? Here’s how you can help:

- 💡 **Suggest Features** – Have an idea to improve the app? [Open a GitHub Issue](../../issues) and share your suggestion.
- 🐞 **Report Issues** – Spotted a bug? [Log it on GitHub](../../issues) so it can be fixed in future updates.
- ⭐ **Support the Project** – If you find this project useful, consider giving it a star to show your support.

## ❤️ Sponsor
The app is open-source, free to use, and contains no ads. Your support helps sustain full-time development.
- 🍵 [Buy Me A Coffee](https://www.buymeacoffee.com/thirumalaikg)
- ☕ [Kofi](https://ko-fi.com/thirumalaikg)
- 💲 [Liberapay](https://en.liberapay.com/thirumalaikg/)
- BTC - `bc1q26ag0k77ez63pd0zu492vj7dncfqmknkk7sjfh`

## 💡 Credits
- [FoodYou](https://github.com/maksimowiczm/FoodYou) - I shamelessly x2 borrowed inspiration from this project.

