# What we want

For each Git root (recursively? -> option `--recurse` / `-r`):
1. Fetch
2. Find production branch candidates and pick latest (we can expand logic later or add options)
3. For checked out branch:
   * If contained in production branch and command line option `--keep` / `-k` not given:
     1. Check out production commit \[_E1_]
     2. Consider this branch as "rest"
   * Otherwise:  
     Merge in production branch \[_E2_]
4. For the rest
   * If contained in production branch:  
     delete

Error cases:
1. Cannot checkout. Ask user to fix and give options: retry, ignore
2. Cannot merge. Ask user to fix and give options: continue, abort, ignore

(a command line option `--ignore` / `-i` seems in order)

There's an auth problem:
* SSH -> no password, but hangs in j**.net
* HTTPS -> need to configure or ask for password. Let's not even think about multiple accounts
