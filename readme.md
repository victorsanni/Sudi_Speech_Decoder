This is a project that uses Viterbi's algorithm to decode parts of speech of words in a sentence.

To natural language speakers (and humans in general), such an exercise would be rather trivial. Everyone knows that 'bird' is a common noun and 'shoot' is a verb. However, there are many situations, even in everyday life, where words have different meanings than what would be expected. 
"School" could mean a school of fish, a place of learning, or just proving to someone that you're better than they are. Larry Bird is one of the most popular basketball players of all time. Will is a request, but is also a popular male first name.
How does a computer know which is which?

With the Viterbi algorithm, a computer can decode the most likely interpretation of a word given its context in a phrase or sentence.
'Will' as a request will usually precede a pronoun or noun (e.g "Will you marry me?"), while Will as a name will most likely precede a verb ("Will is a handsome young man").
Using the Brown corpus, a large dataset containing sentences and part of speech identifiers, my project predicts the most likely parts of speech of the words in any grammatically correct sentence you give it.

<b>Analysis:</b> 

Initially, I tested my algorithm on some test sentences with a small testing dataset.
My algorithm worked with all the sentences and words except one, "You can cook many".
The algorithm failed with the last word (many) in this sentence because the dataset does not recognize the idiomatic form of "many".
Hence, the algorithm just marks "many" as a determiner, assuming a noun would come after and complete the sentence.
After testing the algorithm on a few sentences of my own, I realized the problem: the dataset was too small.
As a result, unusual situations such as a verb starting a sentence ("Cook a fish"),
or consecutive words of not directly connected states ("Can a cook fish", modifier not directly connected to determiner) make the algorithm fail.
With a big enough dataset as the Brown corpus, however, these issues are resolved.

Generally, I discovered that bigger training datasets required bigger penalty constants and vice versa.
For example, with the brown dataset, I noticed that I got errors when my penalty for unseen words was -100 or over.
These errors arose when I specifically used words/sentences unlikely to be in the dataset ("I am the Kwisach Haderach" for example).
With the much smaller simple train dataset, I realized a penalty just below -50 would reduce most errors with unseen words significantly.
I tested my code on the simple-test-sentences. Out of all 32 tags, my code got only 6 tags wrong.
Testing out my code with the Brown dataset, I saw very few errors (although I used very common regular sentences)
Overall, I was impressed with the performance of my code.


