package com.fathomdb.io;

import java.io.IOException;

import com.fathomdb.hash.Sha1Hash;

public class ByteMetadata {
	final ByteSource data;

	public ByteMetadata(ByteSource data) {
		this.data = data;
	}

	Sha1Hash sha1;

	public Sha1Hash sha1() throws IOException {
		if (sha1 == null) {
			sha1 = Sha1Hash.HASHER.hash(data);
		}
		return sha1;
	}

	public static Sha1Hash sha1(ByteSource data) throws IOException {
		ByteMetadata metadata = data.getMetadata();
		return metadata.sha1();
	}

	public void copyFrom(ByteMetadata metadata) {
		if (this.sha1 == null) {
			this.sha1 = metadata.sha1;
		} else {
			if (metadata.sha1 != null) {
				if (!this.sha1.equals(metadata.sha1)) {
					throw new IllegalStateException();
				}
			}
		}
	}

}
