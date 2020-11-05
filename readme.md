# iTunes to Spotify Playlist Converter
Hi there, fellow geek and listener of fine tunes!

If you're like me, you've often pondered whether Spotify or Apple Music was better, and spent a considerable amount of
time updating your playlists on either platform to help make up your mind.

Unfortunately, these large companies with lots of developers on hand haven't built a way to import/export playlists between
their respective platforms, leaving us consumers to manually create and update our music libraries by hand. _Ewww_

Enter this project: a script I spun up to recreate my iTunes/Apple Music playlists on Spotify. Now, with far fewer steps,
we all can move our music around in a faster and easier way _[citation needed]_.

### How it works
This script pulls data from the iTunes Library XML file that is created in your Music folder when enabled in iTunes.
It then queries Spotify's developer API and attempts to re-add the tracks it found in iTunes.

### How to use it
There are a few steps involved, naturally. You though this would be easy?
#### Step 0
Build this project locally, or download the pre-compiled .jar file, and place it in a directory that is easy to access.
Then, create a shell or batch file to run the jar, like so:
```shell script
java -jar itts-1.0.0-jar-with-dependencies.jar process library.xml
```
(We'll create `library.xml` in the next step)
#### Step 1
In iTunes on a PC, go to **Edit > Preferences > Advanced** and tick the checkbox for **Share iTunes Library XML with other applications**.
A new file `iTunes Music Library.xml` should appear in your iTunes music folder, which is at `C:\Users\<you>\Music\iTunes` by default.
Copy this file to the directory you created in the last step, and give it a simpler name, such as `library.xml`.
#### Step 2
Go to https://developer.spotify.com/dashboard/applications and create a new application. In the application settings, you'll 
need to provide a Redirect URI so Spotify will trust us later. This can be any site you wish, as we're going to use it only to
generate an Access Token. If you don't mind Google knowing your token, you can use `https://google.com` as your URI.

Save your changes, and copy down the Client ID of your application.
#### Step 3
We now need to manipulate a URL so we can generate an Access Token (this is getting hacky, I know...). Use the following template
to build a URL with your application's Client ID, as well as the Redirect URI that you gave Spotify:
```
https://accounts.spotify.com/authorize?client_id=CLIENT_ID_HERE&redirect_uri=REDIRECT_URI_HERE&scope=playlist-modify-private%20playlist-read-private%20playlist-modify-public&response_type=token
```
(Replace `CLIENT_ID_HERE` and `REDIRECT_URI_HERE` with your own values)

#### Step 4
It's now time to exude a lot of trust in a stranger's GitHub instructions, and paste the URL you modified into a web browser.
This will first take you to Spotify, where it should ask you to connect the application to your account. You'll want to grant it
the scopes it requested. Once connected, Spotify will redirect you to the URI you provided earlier.

After the redirect, copy the details in your browser's address bar. You should see a URL fragment `#access_token=` followed by
(you guessed it) your Access Token! This is the only piece we need, so you can strip out everything else (including the 
`&token_type=Bearer&expires_in=3600` at the end)

#### Step 5: The easy part (probably)
Now that we have an Access Token and your iTunes Library XML, it's time to run THIS project! Run the shell/batch file from Step 0
(or punch the command into a terminal), and make sure that the final argument is pointing to the XML file.

Eventually, the script will ask for your Access Token. Paste it into the window, and at this point, you can follow the on-screen
instructions. You'll enter the playlist you wish to convert, and it will send a series of API requests to Spotify and make it so.

## Limitations
Currently, this project only performs iTunes -> Spotify transitions, and doesn't work the other way around. Perhaps I will add this
in the future, only fate can tell.

## Contributing
If this project helped you, or if the code offended you by its ugliness, I welcome bug reports and pull requests here on GitHub.
You can also leave a star, because y'know, I could use the self esteem.

Thanks for checking it out!
