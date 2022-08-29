const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();
var name;


exports.PushNotification =
    functions.firestore.document("/Alert_places/{alert}").onCreate(
        (snapshot, context) => {
            // console.log(snapshot)
            name = snapshot.get("user")
            return admin.messaging().sendToTopic("helper", {
                notification: {
                    title: "SafeSpace",
                    body: name + ` needs your help!`,
					sound : "default"
                    }
                }
            )
        }
    );