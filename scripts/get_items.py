import json
import requests


def make_image_url(name):
    return f'https://cdn.stratz.com/images/dota2/items/{name}.png'


result_abilities = []

# items json from stratz api:
# {
# 	constants {
#     items(gameVersionId: 159) {
#       id,
#       shortName
#     }
#   }
# }

with open('items.json') as f:
    items_json = json.load(f)
    for entry in items_json['data']['constants']['items']:
        item_image_response = requests.get(make_image_url(entry['shortName']))
        if item_image_response.status_code != 200:
            print(f'not 200 for {entry["shortName"]}')
            continue

        with open('item_images/' + entry['shortName'] + '.png', 'wb') as img:
            img.write(item_image_response.content)

        print(f'saved {entry["shortName"]}:')
        print()
