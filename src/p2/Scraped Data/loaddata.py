# Simple parse of the 'games.json' file.
import os
import json
import re
import random

def convertDate(s:str) -> str:
    date = s.split()
    if len(date) != 3: return None
    month = date[0].strip()
    day = date[1].rstrip(",").strip()
    if int(day) < 10:
        day = '0' + day
    year = date[2].strip()
    match month:
        case 'Jan':
            month = '01'
        case 'Feb':
            month = '02'
        case 'Mar':
            month = '03'
        case 'Apr':
            month = '04'
        case 'May':
            month = '05'
        case 'Jun':
            month = '06'
        case 'Jul':
            month = '07'
        case 'Aug':
            month = '08'
        case 'Sep':
            month = '09'
        case 'Oct':
            month = '10'
        case 'Nov':
            month = '11'
        case 'Dec':
            month = '12'
    newdate = year + '-' + month + '-' + day
    return newdate

dataset = {}
if os.path.exists('games.json'):
    with open('games.json', 'r', encoding='utf-8') as fin:
        text = fin.read()
        if len(text) > 0:
            dataset = json.loads(text)

games = []
cats = set()
devs = set()
pubs = set()
games_cat = []
games_dev_pubs = []

i = 0
for app in dataset:
    if i >= 100:
        break
    game = dataset[app]

    # Game Info
    gid = app
    date = convertDate(game.get('release_date'))
    if date == None: continue
    rating = round(game.get('positive')/(game.get('positive') + game.get('negative'))*10/2, 2) if (game.get('positive') + game.get('negative')) != 0 else 0
    price = game.get('price', 'NULL')
    name = game.get('name', 'NULL')
    if "'" in name:
        name = name.replace("'", "''")
        
    # Developers
    developers = game['developers']
    for dev in developers: 
        if dev == '': developers.remove('')
    if not developers:  # If developers list is empty, skip this game
        continue
    if len(developers) > 1: continue # For simplicity, we only look for games that has 1 dev

    #Category/Tags/Genre
    genres = game.get('genres', [])

    # Publisher
    publishers = game['publishers']
    for pub in publishers:
        if pub == '': publishers.remove('')
    if not publishers:  # If developers list is empty, skip this game
        continue
    if len(publishers) > 1: continue # Same reason with dev, we only look for games that has 1 pub

    games.append((gid, date, rating, price, name))
    for genre in genres:
        cats.add(genre)
        games_cat.append((gid, genre))
    for developer in developers:
        developer = developer.replace("'", "''")
        devs.add(developer)
    for publisher in publishers:
        publisher = publisher.replace("'", "''")
        pubs.add(publisher)
    i += 1


# Write game data queries
games_file = open("games", "w", encoding='utf-8')
games_file.write("INSERT INTO Game (gid, date, rating, price, gname) VALUES\n")
for i, game in enumerate(sorted(games, key=lambda x:int(x[0]))):
    gid = game[0]
    date = game[1]
    rating = game[2]
    price = game[3]
    name = game[4]
    if i != len(games) - 1:
        games_file.write(f"({gid}, '{date}', {rating}, {price}, '{name}'),\n")
    else:
        games_file.write(f"({gid}, '{date}', {rating}, {price}, '{name}');")
games_file.close()

# Write category data queries
categories_file = open("categories", "w")
categories_file.write("INSERT INTO Category (cname) VALUES\n")
for i, category in enumerate(sorted(cats)):
    if "'" in category:
        category = category.replace("'","''")   
    if i != len(cats) - 1:
        categories_file.write(f"('{category}'),\n")
    else:
        categories_file.write(f"('{category}');")
categories_file.close()

dev_dict = {}
dev_email = {}
email_list = ['@gmail.com', '@outlook.com', '@hotmail.com']

# Write developers data queries
devs_file = open("devs", "w", encoding='utf-8')
devs_file.write("INSERT INTO Developer (did, dname, pemail) VALUES\n")
for i, dev in enumerate(sorted(devs)):
    email = re.sub(r'[^a-zA-Z0-9@._\-\u00C0-\u024F\u2E80-\u9FFF\uAC00-\uD7AF]', '', dev).lower() + email_list[random.randint(0,len(email_list)-1)]
    dev_email[dev] = email
    if i != len(devs) - 1:
        devs_file.write(f"({i}, '{dev}', '{email}'),\n")
        dev_dict[dev] = i
    else:
        devs_file.write(f"({i}, '{dev}', '{email}');")
        dev_dict[dev] = i
devs_file.close()

pub_dict = {}

# Write publisher data queries
pubs_file = open("pubs", "w", encoding='utf-8')
pubs_file.write("INSERT INTO Publisher (pid, pname, pemail) VALUES\n")
for i, pub in enumerate(sorted(pubs)):
    if dev_email.get(pub) != None:
        email = dev_email.get(pub)
    else:
        email = re.sub(r'[^a-zA-Z0-9@._\-\u00C0-\u024F\u2E80-\u9FFF\uAC00-\uD7AF]', '', pub).lower() + email_list[random.randint(0,len(email_list)-1)]
    if i != len(pubs) - 1:
        pubs_file.write(f"({i}, '{pub}', '{email}'),\n")
        pub_dict[pub] = i
    else:
        pubs_file.write(f"({i}, '{pub}', '{email}');")
        pub_dict[pub] = i
pubs_file.close()

# Write BelongsTo queries
belongs_file = open("belongs", "w", encoding='utf-8')
belongs_file.write("INSERT INTO BelongsTo (gid, cname) VALUES\n")
for i, belongs in enumerate(sorted(games_cat, key=lambda x: x[0])):
    gid = belongs[0]
    cat = belongs[1]
    if i != len(games_cat) - 1:
        belongs_file.write(f"({gid}, '{cat}'),\n")
    else:
        belongs_file.write(f"({gid}, '{cat}');")
belongs_file.close()

# Write MakePublish queries
makepublish_file = open("makepublish", "w", encoding='utf-8')
makepublish_file.write("INSERT INTO MakePublish (gid, did, pid) VALUES\n")
for i, g in enumerate(sorted(games, key=lambda x:int(x[0]))):
    game = dataset[g[0]]
    dev = game['developers'][0].replace("'", "''")
    pub = game['publishers'][0].replace("'", "''")
    gid = g[0]
    did = dev_dict.get(dev)
    pid = pub_dict.get(pub)
    if i != len(games) - 1:
        makepublish_file.write(f"({gid}, {did}, {pid}),\n")
    else:
        makepublish_file.write(f"({gid}, {did}, {pid});")