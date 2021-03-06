TEST_MODE = True
TOP_K = 10
TRAIN_LEN_RNN = 6 + 1
NGRAM_EXCODE_PARAM = 3 + 1  # 4-gram
NGRAM_LEXICAL_PARAM = 3 + 1  # 4-gram
NGRAM_EXCODE_METHODCALL = NGRAM_EXCODE_PARAM
NGRAM_LEXICAL_METHODCALL = NGRAM_LEXICAL_PARAM
NGRAM_SCORE_WEIGHT = 1
GPT_BATCH_SIZE = 3  # Set to 1 if PARAM_LEXICAL_ONLY is set to False
GPT_CONTEXT_LEN = 1000
GPT_TEMPERATURE = 1
GPT_TOP_K = 0
GPT_TOP_P = 0.85
USE_EXCODE_MODEL = True
USE_JAVA_MODEL = True
USE_METHOD_CALL_MODEL = False
USE_RNN = False
USE_NGRAM = True
USE_GPT = False
USE_PROGRAM_ANALYSIS = True
USE_LEXSIM = True
USE_LOCAL_VAR = False
PARAM_LEXICAL_ONLY = False

LEXSIM_MULTIPLIER = 1
LEXSIM_SMALL_PENALTY = -3.32193  # log2(0.1)
LOCAL_VAR_BONUS = 1  # log2(2)

LOG_ZERO = -32

PRINT_LOG = False

PROJECT = 'all'
TESTFOLD = 0
# excode_model_rnn_path = "../../../../../model/excode_model_" + project + "_testfold_" + str(testfold) + ".h5"
# java_model_rnn_path = "../../../../../model/java_model_" + project + "_testfold_" + str(testfold) + ".h5"
METHODCALL_MODEL_RNN_PATH = "../../../../../model/v4-10fold-method/" + str(NGRAM_EXCODE_METHODCALL) + " gram/method_call_model_" \
                             + PROJECT + "_testfold_" + str(TESTFOLD) + "_rnn.h5"
EXCODE_MODEL_RNN_PATH = "../../../../../model/excode_model_all.h5"
JAVA_MODEL_RNN_PATH = "../../../../../model/java_model_all.h5"
METHODCALL_MODEL_NGRAM_PATH = "../../../../../model/v4-10fold/" + str(NGRAM_LEXICAL_METHODCALL) + " gram/method_call_model_" \
                              + PROJECT + "_testfold_" + str(TESTFOLD) + "_ngram.pkl"
EXCODE_MODEL_NGRAM_PATH = "../../../../../model/v5-eclipse-netbeans-whole/netbeans/4 gram/excode_model_netbeans_tokens_testfold_0full_vocab_ngram.pkl"
JAVA_MODEL_NGRAM_PATH = "../../../../../model/v5-eclipse-netbeans-whole/netbeans/4 gram/java_model_netbeans_tokens_testfold_0full_vocab_ngram.pkl"
METHODCALL_MODEL_GPT_PATH = "../../../../../model/gpt/method_call/"
EXCODE_MODEL_GPT_PATH = "../../../../../model/gpt/excode/"
JAVA_MODEL_GPT_PATH = "../../../../../model/gpt/java/"
EXCODE_TOKENIZER_PATH = '../../../../src/main/python/model/excode/excode_tokenizer'
JAVA_TOKENIZER_PATH = '../../../../src/main/python/model/java/java_tokenizer'
METHODCALL_TOKENIZER_PATH = '../../../../src/main/python/model/method_call/method_call_eclipse_swt_tokenizer_3'
EXCODE_TOKENS_PATH = '../../../../data_dict/excode/excode_tokens_n_symbols.txt'
