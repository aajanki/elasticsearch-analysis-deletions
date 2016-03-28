Fuzzy query analyzer for Elasticsearch
======================================

Requires Elasticsearch 2.2.x

This plugin provides analyzer and token filter that generate
misspelled word variants. This can be used to implement "fuzzy"
queries or queries that are tolerant to small spelling errors in
search terms.

Homepage: https://github.com/aajanki/elasticsearch-analysis-deletions

## Installation ##

### 1. Build ###
	
In the plugin folder, run `mvn test` to run the tests. Run `mvn
package` to build the jar.

### 2. Install ###
	
Extract the zip file from the `target/releases` directory to
`/usr/share/elasticsearch/plugins/analysis-deletions/`.
Restart the ElasticSearch service.
	
### 3. Use ###

Testing with the `_analyze` endpoint:

```
curl -XPOST 'localhost:9200/_analyze?pretty' -d '{
  "analyzer": "deletions",
  "text": "Red Hat"
}'
```

Create a mapping:

```
curl -XPOST 'localhost:9200/myindex2' -d '{
    "mappings": {
        "mytype": {
            "properties": {
                "mytext": {
                    "type": "string",
                    "analyzer": "deletions"
                }
            }
        }
    }
}'
```

By default, the deletions analyzer uses the standard tokenizer and the
lowercase and deletions token filters and generates all one letter
deletions. The following shows how to configure a different tokenizer
and a larger edit distance:

```
curl -XPOST 'localhost:9200/myindex2' -d '{
    "mappings": {
        "mytype": {
            "properties": {
                "mytext": {
                    "type": "string",
                    "analyzer": "misspelling"
                }
            }
        }
    },
    "settings": {
        "analysis": {
            "filter": {
                "deletions_2_filter": {
                    "type": "deletions",
                    "distance": 2
                }
            },
            "analyzer": {
                "misspelling": {
                    "tokenizer": "whitespace",
                    "filter": ["deletions_2_filter"]
                }
            }
        }
    }
}'
```

Fetch documents using a match query:

```
curl -XPOST 'localhost:9200/myindex/_search?pretty' -d '{
    "query": {
        "match": {
            "mytext": "misspeled serach termes"
        }
    }
}'
```

## Relation to fuzzy queries ##

Elasticsearch already provides [fuzzy matching
queries](https://www.elastic.co/guide/en/elasticsearch/reference/2.2/query-dsl-match-query.html#query-dsl-match-query-fuzziness).
Why create an analyzer for the same purpose? This is an experiment to
see how much fuzzy queries can be sped up by precomputing misspelling
variants at the indexing time.

The standard fuzzy query in Elasticsearch generates all variants of
search terms up to the maximum allowed edit distance, and then finds
out which of those exist in the index. These variants include all
possible deletions, insertions and transpositions of letters. The
number of variants can be quite large. Fuzzy queries can be somewhat
slow (even though [Lucene does some interesting
optimizations](http://blog.mikemccandless.com/2011/03/lucenes-fuzzyquery-is-100-times-faster.html)).

This plugin computes edit variants already at the indexing time, and
indexes them as synonyms for the original words. At the query time,
the same analyzer is applied to the search terms. The analyzer
generates only the deletion variants. This keeps the index size from
exploding and should result in faster queries because less variants
need to be generated.

According to my small-scale testing, this approach results in slightly
(up to 30 %) faster queries. The main disadvantage is the increased
number of unique terms that need to be indexed.
