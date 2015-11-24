# For Local
./mongoimport --db doctornearby --collection doctor --type json --file ~/Documents/workspaceT/doctorrobot/json/doctors_20151016.json --jsonArray

# For Server
./mongoimport -h ds039684.mongolab.com:39684 -u doctornearby -p 330zhangZHEhan -d doctornearby -c doctor --type json --file ~/Documents/workspaceT/doctorrobot/json/doctors_final_on.json --jsonArray