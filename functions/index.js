const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.PushNotification =
    functions.firestore.document("/Alert_places/{alert}").onCreate(
        (snapshot, context) =>
        {
            return admin.messaging().sendToTopic("helper",{
                    notification: {
                        title: "SafeSpace",
                        body: "Someone around you needs your help!",
						sound : "default"
                    }
                }
            )
        }
    );