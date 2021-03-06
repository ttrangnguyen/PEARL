import os
from name_stat.name_tokenizer import tokenize
from keras.preprocessing.sequence import pad_sequences
from pickle import load
import csv
from pathlib import Path


def read_file(filepath):
    with open(filepath) as f:
        str_text = f.read()
    return str_text


def prepare_sequence(sequence, train_len):
    return pad_sequences(sequence, maxlen=train_len, padding='pre')


def prepare_sequence(sequence, train_len, *args):
    padded_sequences = pad_sequences(sequence, maxlen=train_len, padding='pre')
    rows = []
    for i in range(len(padded_sequences)):
        row = list(padded_sequences[i])
        for j in range(len(args)):
            row.append(args[j][i])
        rows.append(row)
    return rows


def java_tokenize_take_last(lines, tokenizer, train_len):
    text_sequences = []
    all_tokens = []
    for line in lines:
        stripped = line.strip()
        if stripped == '':
            continue
        if 'a' <= stripped[0] <= 'z' or 'A' <= stripped[0] <= 'Z':
            all_tokens += tokenize(stripped)
        else:
            all_tokens += [stripped]
    # print(all_tokens)
    seq = all_tokens[max(len(all_tokens) - train_len, 0):len(all_tokens)]
    text_sequences.append(seq)
    sequences = tokenizer.texts_to_sequences(text_sequences)
    return sequences[0]


def java_tokenize(text, tokenizer, train_len, last_only=False):
    text_sequences = []
    text_class_names = []
    text_method_names = []
    all_tokens = []
    method_name = ""
    class_name = None
    lines = text.split("\n")
    class_level = 0
    method_level = 0
    # print(lines)
    n = len(lines)
    i = 0
    while i < n:
        if lines[i] == '`':
            # Class begin
            class_level += 1
            if class_level == 1:
                class_name = tokenize(lines[i + 1])
            i += 2
        elif lines[i] == '¬':
            class_level -= 1
            i += 1
        elif lines[i] == '#':
            method_level += 1
            if method_level == 1:
                method_name = tokenize(lines[i + 1])
            i += 2
        elif lines[i] == '$':
            method_level -= 1
            if method_level == 0 and class_level > 0:
                for j in range(1, len(all_tokens)):
                    seq = all_tokens[max(j - train_len, 0):j]
                    text_sequences.append(seq)
                    text_method_names.append(method_name)
                    text_class_names.append(class_name)
            all_tokens = []
            i += 1
        elif method_level > 0:
            stripped = lines[i].strip()
            if stripped != '':
                if 'a' <= stripped[0] <= 'z' or 'A' <= stripped[0] <= 'Z':
                    all_tokens += tokenize(stripped)
                else:
                    all_tokens += [stripped]
            i += 1
        else:
            i += 1
    # print(all_tokens)
    sequences = tokenizer.texts_to_sequences(text_sequences)
    method_names_tokens = tokenizer.texts_to_sequences(text_method_names)
    class_names_tokens = tokenizer.texts_to_sequences(text_class_names)
    return sequences, method_names_tokens, class_names_tokens


def java_tokenize_sentences(lexes, tokenizer, to_sequence=True):
    text_sequences = []
    for lex in lexes:
        all_tokens = []
        stripped = lex.strip()
        if stripped == '':
            continue
        if 'a' <= stripped[0] <= 'z' or 'A' <= stripped[0] <= 'Z':
            all_tokens += tokenize(stripped)
        else:
            all_tokens += [stripped]
        text_sequences.append(all_tokens)
    if to_sequence:
        sequences = tokenizer.texts_to_sequences(text_sequences)
        return sequences
    else:
        return text_sequences


def load_java_tokenizer():
    tokenizer = load(open('java_tokenizer', 'rb'))
    return tokenizer


def preprocess(train_path, csv_path, train_len):
    tokenizer = load_java_tokenizer()

    with open(csv_path, 'w', newline='') as java_csv:
        writer = csv.writer(java_csv)
        index = []
        for i in range(train_len - 1):
            index.append("input{}".format(i))
        index.append("label")
        index.append("method_name")
        index.append("class_name")
        writer.writerow(index)
        for f in os.listdir(train_path):
            # print(f)
            text = read_file(os.path.join(train_path, f))
            sequences, method_names_tokens, class_names_tokens = java_tokenize(text, tokenizer, train_len)
            writer.writerows(prepare_sequence(sequences, train_len, method_names_tokens, class_names_tokens))

    import pandas as pd
    df = pd.read_csv(csv_path)
    cols = list(df.columns)
    cols[:train_len] = ["label"] + cols[:train_len - 1]
    df_reorder = df[cols]
    df_reorder.to_csv(csv_path, index=False)


def listdirs(folder):
    return [d for d in os.listdir(folder) if os.path.isdir(os.path.join(folder, d))]


if __name__ == '__main__':
    data_types = ['fold_' + str(x) for x in range(10)]
    data_parent_folders = ['data_csv_3_gram']
    train_len = [2 + 1]
    version = '3'
    n_folds = 10
    data_version_path = '../../../../../../data_v' + version + '/'

    for data_type in data_types:
        for i in range(len(data_parent_folders)):
            projects_path = data_version_path + 'data_classform/java' + '/' \
                            + str(n_folds) + '_folds' + '/' + data_type + '/'
            projects = listdirs(projects_path)
            for project in projects:
                Path(data_version_path + data_parent_folders[i] + '/java/' + project). \
                    mkdir(parents=True, exist_ok=True)
                preprocess(train_path=projects_path + project + '/',
                           csv_path=data_version_path + data_parent_folders[i] + '/java/' + project + '/java_' +
                                    data_type + "_" + project + '.csv',
                           train_len = train_len[i])
