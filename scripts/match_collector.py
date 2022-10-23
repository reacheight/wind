import requests as r
import sys

HERO_ID = sys.argv[1]
starting_date = '1666024560'

def build_request_url(hero_id, start_time, limit = 1000):
    return f'https://api.opendota.com/api/explorer?sql=select m.match_id, m.start_time from public_player_matches p join public_matches m on p.match_id = m.match_id where p.hero_id = {hero_id} and m.avg_rank_tier > 60 and m.start_time < {start_time} limit {limit}'

matches = []

current_start_time = starting_date
while len(matches) < 50000:
    url = build_request_url(HERO_ID, current_start_time)
    response = r.get(url)

    if response.status_code != r.codes.ok:
        print(f'Error for {current_start_time}. Code: {response.status_code}, Content: {response.content}.')
        continue
    
    result = response.json()
    result_matches = result['rows']

    matches += [str(i['match_id']) for i in result_matches]
    current_start_time = result_matches[-1]['start_time']

    print(f'Success for {current_start_time}. Added {len(result_matches)} matches.')

with open(f'{HERO_ID}.txt', 'w') as output:
    for match in matches:
        output.write(f'{match}\n')
