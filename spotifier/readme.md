# Spotifier

_Hack your own Spotify account to keep track of changes over time_

Suppose you want to see how your yearly top playlists have evolved over time;
Suppose you wish to see how
Suppose you put time and sweat into some playlists and would like to preserve them in the event Spotify itself vanishes;

then you can use this absolute hack of a repo, hack your own account, and extract such data!

### Steps:

1. Be sufficiently technical to know about Maven and have an IDE
2. Clone this whole repo (but pay no mind to stuff outside `spotifier`) and do whatever unforeseeable doings are needed to make the IDE and compiler happy
3. Modify the paths at the top of `Coordinator` to suit your desire, especially the first 3
4. Modify `main` to only call `Coordinator.createDirectoiesAndFiles()` and run it once
5. You'll now have two files `authorization` and `client-token` you need to fill in. For this:
   1. Go to Spotify in your browser (and login if needed)
   2. Open Developer Tools (F12 usually) and go to the Network tab
   3. Navigate around Spotify a bit until you see a call to `api-partner.spotify.com`
   4. Inspect the call and find requestheaders `authorization` and `client-token`
   5. Copy their values into the two respective files.
6. 