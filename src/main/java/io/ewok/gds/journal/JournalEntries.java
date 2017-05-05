package io.ewok.gds.journal;

import io.ewok.gds.journal.messages.JournalEntry;
import io.ewok.gds.journal.messages.TxnAbort;
import io.ewok.gds.journal.messages.TxnAcctEntry;
import io.ewok.gds.journal.messages.TxnBegin;
import io.ewok.gds.journal.messages.TxnCommit;

public class JournalEntries {

	private static JournalEntry txnAcctRM(TxnAcctEntry rm) {
		return JournalEntry.newBuilder().setTxnAcct(rm).build();
	}

	public static JournalEntry txnBegin(int txnId) {
		return txnAcctRM(TxnAcctEntry.newBuilder().setTxnBegin(TxnBegin.newBuilder().setTxnId(txnId).build()).build());
	}

	public static JournalEntry txnCommit(int txnId) {
		return txnAcctRM(
				TxnAcctEntry.newBuilder().setTxnCommit(TxnCommit.newBuilder().setTxnId(txnId).build()).build());
	}

	public static JournalEntry txnAbort(int txnId) {
		return txnAcctRM(TxnAcctEntry.newBuilder().setTxnAbort(TxnAbort.newBuilder().setTxnId(txnId).build()).build());
	}

}
