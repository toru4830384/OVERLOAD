package overload_api.auth;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
/** nonceの使い回しと、別アカウントへ戻るGoogle連携を拒否する。 */
class GoogleChallengeStoreTest {
    @Test void challengeIsSingleUseAndBoundToPurposeAndAccount() {
        var store=new GoogleChallengeStore();var session=new MockHttpSession();
        var user=new UserIdentity(1L,"user_one","namespace",0);
        String nonce=store.issue(session,"link",user);
        assertFalse(nonce.isBlank());assertEquals(nonce,store.consume(session,"link",user));
        assertThrows(IllegalArgumentException.class,()->store.consume(session,"link",user));
        store.issue(session,"link",user);
        assertThrows(IllegalArgumentException.class,()->store.consume(session,"login",null));
        store.issue(session,"link",user);
        assertThrows(IllegalArgumentException.class,()->store.consume(session,"link",new UserIdentity(2L,"user_two","other",0)));
        store.issue(session,"link",user);
        assertThrows(IllegalArgumentException.class,()->store.consume(session,"link",new UserIdentity(1L,"user_one","namespace",1)));
    }
    @Test void expiredChallengeAndPendingRegistrationAreRejected() {
        class MutableClock extends java.time.Clock {
            long millis=1000;
            public java.time.ZoneId getZone(){return java.time.ZoneOffset.UTC;}
            public java.time.Clock withZone(java.time.ZoneId zone){return this;}
            public java.time.Instant instant(){return java.time.Instant.ofEpochMilli(millis);}
        }
        var clock=new MutableClock();var store=new GoogleChallengeStore(clock);var session=new MockHttpSession();
        store.issue(session,"login",null);store.pending(session,"subject");clock.millis+=300001;
        assertThrows(IllegalArgumentException.class,()->store.consume(session,"login",null));
        assertThrows(IllegalArgumentException.class,()->store.pendingSubject(session));
    }
    @Test void pendingProofIsClearedByLoginOrNewChallenge() {
        var store=new GoogleChallengeStore();var session=new MockHttpSession();
        assertFalse(store.hasPending(null));store.pending(session,"subject");assertTrue(store.hasPending(session));
        store.issue(session,"login",null);assertFalse(store.hasPending(session));
        store.pending(session,"subject");store.clear(session);assertFalse(store.hasPending(session));
    }
}
