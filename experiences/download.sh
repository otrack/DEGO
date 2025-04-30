#!/bin/bash
set -e

declare -A FILES
FILES["graph_following_retwis_100000_users.txt"]="1SvLvpwTK7YAW_IlkFNG6R0DXXPe48ZA1"
FILES["graph_following_retwis_500000_users.txt"]="1RcgbTH9Asdgr2p97TUO-okDn6cc59nDA"
FILES["graph_following_retwis_1000000_users.txt"]="1UylhOGUY9jvLoxwfpJB1Ne_9EbZR0MFb"

for filename in "${!FILES[@]}"; do
    if [ ! -f "$filename" ]; then
        echo "📥 Téléchargement de $filename..."
        gdown --id "${FILES[$filename]}" -O "$filename"
    else
        echo "✅ $filename déjà présent."
    fi
done

# Lance ensuite le script principal (ou toute commande passée en argument)
exec "$@"
