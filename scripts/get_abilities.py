import json
import requests


def make_image_url(name):
    return f'https://cdn.stratz.com/images/dota2/abilities/{name}.png'


result_abilities = []

# abilities json from stratz api:
# {
# 	constants {
#     abilities(gameVersionId: 157) {
#       id,
#       name,
#       isTalent,
#       stat {
#         isGrantedByShard,
#         isGrantedByScepter,
#         isUltimate,
#       }
#     }
#   }
# }

with open('abilities.json') as f:
    abilities_json = json.load(f)
    for entry in abilities_json['data']['constants']['abilities']:
        if entry['isTalent'] or entry['stat'] is None:
            continue

        ability_image_response = requests.get(make_image_url(entry['name']))
        if ability_image_response.status_code != 200:
            continue

        with open('images/' + entry['name'] + '.png', 'wb') as img:
            img.write(ability_image_response.content)

        ability = {
            'id': entry['id'],
            'name': entry['name'],
            'isGrantedByShard': entry['stat']['isGrantedByShard'],
            'isGrantedByScepter': entry['stat']['isGrantedByScepter'],
            'isUltimate': entry['stat']['isUltimate']
        }

        result_abilities.append(ability)
        print(f'saved {ability["name"]}:')
        print(ability)
        print()

result_json = {
    'abilities': result_abilities
}


with open('abilities.json', 'w') as result_json_file:
    result_json_file.write(json.dumps(result_json))
