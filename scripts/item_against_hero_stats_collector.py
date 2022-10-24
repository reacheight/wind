import sys
import math

class ItemAgainstHeroDataEntry:
    def __init__(self, is_hero_radiant, radiant_has_item, dire_has_item, radiant_networth, dire_networth, radiant_won, is_stomp):
        self.is_hero_radiant = is_hero_radiant
        self.is_hero_dire = not is_hero_radiant
        self.radiant_has_item = radiant_has_item
        self.dire_has_item = dire_has_item
        self.radiant_networth = radiant_networth
        self.dire_networth = dire_networth
        self.radiant_won = radiant_won
        self.is_stomp = is_stomp

    def is_hero_win(self):
        return self.is_hero_radiant and self.radiant_won or self.is_hero_dire and (not self.radiant_won)

    def any_team_has_item(self):
        return self.radiant_has_item or self.dire_has_item

    def is_item_win(self):
        return self.radiant_has_item and self.radiant_won or self.dire_has_item and (not self.radiant_won)
    
    def is_item_against_hero(self):
        return self.is_hero_radiant and self.dire_has_item or self.is_hero_dire and self.radiant_has_item
    
    def is_item_win_against_hero(self):
        return (not self.is_hero_win()) and self.is_item_win()

def parse_line(line):
    tokens = line.split()
    return ItemAgainstHeroDataEntry(tokens[0] == "1", tokens[2] == "1", tokens[3] == "1", int(tokens[4]), int(tokens[5]), tokens[6] == "1", tokens[7] == "1")

def percentile(data, perc: int):
    if perc == 0:
        return min(data)
        
    size = len(data)
    return sorted(data)[int(math.ceil((size * perc) / 100)) - 1]

data = []
with open(sys.argv[1], 'r') as input:
    data = [parse_line(line) for line in input.readlines()]


matches_with_item = [entry for entry in data if entry.any_team_has_item()]
item_against_hero_matches = [entry for entry in data if entry.is_item_against_hero()]
no_item_against_hero_matches = [entry for entry in data if not entry.is_item_against_hero()]

teams_with_item_against_hero_networths = []
for entry in item_against_hero_matches:
    if entry.dire_has_item and entry.is_hero_radiant:
        teams_with_item_against_hero_networths.append(entry.dire_networth)
    
    if entry.radiant_has_item and entry.is_hero_dire:
        teams_with_item_against_hero_networths.append(entry.radiant_networth)

item_networth_quantiles = [percentile(teams_with_item_against_hero_networths, i * 10) for i in range(10)]

def calculate_winrate_against_hero(matches):
    match_count = []
    win_count = []
    for networth_quantiles in item_networth_quantiles:
        count = 0
        wins = 0
        for entry in matches:
            if entry.radiant_networth >= networth_quantiles and entry.dire_networth >= networth_quantiles:
                count += 1

                if not entry.is_hero_win():
                    wins += 1
                    
        match_count.append(count)
        win_count.append(wins)
    
    return (match_count, win_count)

item_against_hero_match_count, item_against_hero_wins_count = calculate_winrate_against_hero(item_against_hero_matches)
no_item_against_hero_match_count, no_item_against_hero_wins_count = calculate_winrate_against_hero(no_item_against_hero_matches)

total_match_count = []
for networth_quantiles in item_networth_quantiles:
    count = 0
    for entry in data:
        if entry.radiant_networth >= networth_quantiles and entry.dire_networth >= networth_quantiles:
            count += 1
    total_match_count.append(count)


print("Match count:")
for i in range(10):
    print(f'    Both teams have >= {i * 10} percentile of item networths:')
    print(f'        Match count = {total_match_count[i]}')

print()

print("Item against hero:")
for i in range(10):
    print(f'    Both teams have >= {i * 10} percentile of item networths:')
    print(f'        Match count            = {item_against_hero_match_count[i]}')
    print(f'        Win against hero count = {item_against_hero_wins_count[i]}')
    print(f'        Winrate                = {item_against_hero_wins_count[i] / item_against_hero_match_count[i]}')

print()

print("No item against hero:")
for i in range(10):
    print(f'    Both teams have >= {i * 10} percentile of item networths:')
    print(f'        Match count            = {no_item_against_hero_match_count[i]}')
    print(f'        Win against hero count = {no_item_against_hero_wins_count[i]}')
    print(f'        Winrate                = {no_item_against_hero_wins_count[i] / no_item_against_hero_match_count[i]}')

no_stomp_matches = [entry for entry in data if not entry.is_stomp]
stomp_count = len(data) - len(no_stomp_matches)

no_stomp_item_matches = [entry for entry in no_stomp_matches if entry.is_item_against_hero()]
no_stomp_item_win_count = len([entry for entry in no_stomp_item_matches if not entry.is_hero_win()])

no_stomp_no_item_matches = [entry for entry in no_stomp_matches if not entry.is_item_against_hero()]
no_stomp_no_item_win_count = len([entry for entry in no_stomp_no_item_matches if not entry.is_hero_win()])

print()
print(f'Total matches: {len(data)}. Stomp matches: {stomp_count}.')
print()
print(f'No stomp Item against hero matches count = {len(no_stomp_item_matches)}')
print(f'No stomp Item against hero win count     = {no_stomp_item_win_count}')
print(f'Winrate:                                 = {no_stomp_item_win_count / len(no_stomp_item_matches)}')
print()
print(f'No stomp No Item against hero matches count = {len(no_stomp_no_item_matches)}')
print(f'No stomp No Item against hero win count     = {no_stomp_no_item_win_count}')
print(f'Winrate:                                    = {no_stomp_no_item_win_count / len(no_stomp_no_item_matches)}')