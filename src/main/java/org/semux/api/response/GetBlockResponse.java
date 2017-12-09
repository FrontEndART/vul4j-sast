/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.api.response;

import org.semux.core.Block;
import org.semux.crypto.Hex;
import org.semux.util.TimeUtil;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetBlockResponse extends ApiHandlerResponse {

    @JsonProperty("result")
    public final Result block;

    public GetBlockResponse(
            @JsonProperty("success") Boolean success,
            @JsonProperty("result") Result block) {
        super(success, null);
        this.block = block;
    }

    public static class Result {

        @JsonProperty("hash")
        public final String hash;

        @JsonProperty("number")
        public final Long number;

        @JsonProperty("coinbase")
        public final String coinbase;

        @JsonProperty("prevHash")
        public final String prevHash;

        @JsonProperty("timestamp")
        public final Long timestamp;

        @JsonProperty("date")
        public final String date;

        @JsonProperty("transactionsRoot")
        public final String transactionsRoot;

        @JsonProperty("resultsRoot")
        public final String resultsRoot;

        @JsonProperty("stateRoot")
        public final String stateRoot;

        @JsonProperty("data")
        public final String data;

        public Result(
                @JsonProperty("hash") String hash,
                @JsonProperty("number") Long number,
                @JsonProperty("coinbase") String coinbase,
                @JsonProperty("prevHash") String prevHash,
                @JsonProperty("timestamp") Long timestamp,
                @JsonProperty("date") String date,
                @JsonProperty("transactionsRoot") String transactionsRoot,
                @JsonProperty("resultsRoot") String resultsRoot,
                @JsonProperty("stateRoot") String stateRoot,
                @JsonProperty("data") String data) {
            this.hash = hash;
            this.number = number;
            this.coinbase = coinbase;
            this.prevHash = prevHash;
            this.timestamp = timestamp;
            this.date = date;
            this.transactionsRoot = transactionsRoot;
            this.resultsRoot = resultsRoot;
            this.stateRoot = stateRoot;
            this.data = data;
        }

        public Result(Block block) {
            this(
                    Hex.encodeWithPrefix(block.getHash()),
                    block.getNumber(),
                    Hex.encodeWithPrefix(block.getCoinbase()),
                    Hex.encodeWithPrefix(block.getPrevHash()),
                    block.getTimestamp(),
                    TimeUtil.formatTimestamp(block.getTimestamp()),
                    Hex.encodeWithPrefix(block.getTransactionsRoot()),
                    Hex.encodeWithPrefix(block.getResultsRoot()),
                    Hex.encodeWithPrefix(block.getStateRoot()),
                    Hex.encodeWithPrefix(block.getData())
            );
        }
    }
}
