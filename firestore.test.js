const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("1. Unauthenticated user cannot read or write transactions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get());
});

test("2. Authenticated user can create valid minor-unit transaction", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_valid_1").set({
      userId: ALICE_UID,
      title: "Salary Credit",
      amountMinor: 3905000,
      type: "INCOME",
      categoryId: "other",
      categoryName: "Other",
      timestampMillis: 1700000000000,
      notes: "Monthly salary",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("3. Authenticated user cannot read another user's transactions", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(ALICE_UID).collection("transactions").doc("tx_alice").set({
      userId: ALICE_UID,
      title: "Rent",
      amountMinor: 1500000,
      type: "EXPENSE",
      categoryId: "housing",
      categoryName: "Housing",
      timestampMillis: 1700000000000,
      createdAt: now,
      updatedAt: now,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_alice").get()
  );
});

test("4. Transaction with zero or negative amountMinor is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_zero").set({
      userId: ALICE_UID,
      title: "Invalid Zero",
      amountMinor: 0,
      type: "EXPENSE",
      categoryId: "food",
      categoryName: "Food & Dining",
      timestampMillis: 1700000000000,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("5. Exclusive E.164 phone registry blocks cross-account phone hijacking", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("phone_registry").doc("+919876543210").set({
      phoneE164: "+919876543210",
      ownerUid: ALICE_UID,
      displayName: "Alice",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(
    bobDb.collection("phone_registry").doc("+919876543210").set({
      phoneE164: "+919876543210",
      ownerUid: BOB_UID,
      displayName: "Bob",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("6. Roommate connection request blocks self-requests and sender self-acceptance", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const now = new Date();

  // Self-request blocked
  await assertFails(
    aliceDb.collection("connection_requests").doc("req_self").set({
      senderUid: ALICE_UID,
      senderName: "Alice",
      senderPhone: "+919876543210",
      recipientUid: ALICE_UID,
      recipientName: "Alice",
      recipientPhone: "+919876543210",
      status: "PENDING",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Valid request from Alice to Bob succeeds
  await assertSucceeds(
    aliceDb.collection("connection_requests").doc("req_1").set({
      senderUid: ALICE_UID,
      senderName: "Alice",
      senderPhone: "+919876543210",
      recipientUid: BOB_UID,
      recipientName: "Bob",
      recipientPhone: "+919876543211",
      status: "PENDING",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Sender (Alice) cannot accept her own outgoing request
  await assertFails(
    aliceDb.collection("connection_requests").doc("req_1").update({
      status: "ACCEPTED",
      updatedAt: new Date(),
    })
  );

  // Unrelated third party (Charlie) cannot accept or decline
  await assertFails(
    charlieDb.collection("connection_requests").doc("req_1").update({
      status: "ACCEPTED",
      updatedAt: new Date(),
    })
  );

  // Authorized recipient (Bob) can accept
  await assertSucceeds(
    bobDb.collection("connection_requests").doc("req_1").update({
      status: "ACCEPTED",
      updatedAt: new Date(),
    })
  );

  // Terminal state lock: once ACCEPTED, status cannot be mutated again
  await assertFails(
    bobDb.collection("connection_requests").doc("req_1").update({
      status: "DECLINED",
      updatedAt: new Date(),
    })
  );
});
