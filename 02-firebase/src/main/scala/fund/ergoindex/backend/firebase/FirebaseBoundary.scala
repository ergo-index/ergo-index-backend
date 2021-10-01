package fund.ergoindex.backend
package firebase

import cats.effect.IO

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import com.google.firebase.auth.{FirebaseAuth, FirebaseAuthException, FirebaseToken}

import scala.util.Try

trait FirebaseBoundary:
  def init(): IO[FirebaseApp]
  def verifyIdToken(idToken: String, app: FirebaseApp): IO[Try[FirebaseToken]];

object FirebaseBoundary:
  def make(): FirebaseBoundary = new:
    override def init(): IO[FirebaseApp] = IO {
      val options = FirebaseOptions
        .builder()
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build();

      FirebaseApp.initializeApp(options);
    }

    override def verifyIdToken(idToken: String, app: FirebaseApp) = IO {
      val auth = FirebaseAuth.getInstance(app)
      Try(auth.verifyIdToken(idToken))
    }
