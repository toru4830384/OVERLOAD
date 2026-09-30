import React, { useState, useEffect, useMemo, useCallback } from "react";
import { Plus, Play, Pause, Check, X, ChevronDown, ChevronUp, ChevronLeft, Dumbbell, History, ListChecks, Timer, Trash2, User } from "lucide-react";

const COLORS = {
  bg: "#15171A",
  surface: "#1E2124",
  surfaceAlt: "#262A2E",
  line: "#33383D",
  chalk: "#F1EFEA",
  steel: "#9AA5AD",
  steelDim: "#6C767D",
  blue: "#2E6FD9",   // 20kg
  yellow: "#D6B62B", // 15kg
  green: "#2BAA5E",  // 10kg
  white: "#EDEBE6",  // 5kg
  black: "#3A3D40",  // 2.5kg
  chrome: "#9AA5AD", // 1.25kg
  red: "#D6402B",
  amber: "#F0A82E",  // hazard/warning accent
};

const HAZARD_STRIPE = `repeating-linear-gradient(135deg, #F0A82E, #F0A82E 8px, #15171A 8px, #15171A 16px)`;

const BAR_WEIGHT = 20;

const DEFAULT_EXERCISES = [
  { id: "ex-bench", name: "ベンチプレス", category: "胸", barbell: true, equipment: "barbell", pattern: "push_horizontal", muscle: "大胸筋(胸肋部・中部)" },
  { id: "ex-squat", name: "スクワット", category: "腿前", barbell: true, equipment: "barbell", pattern: "squat", muscle: "大腿四頭筋(大腿直筋・内側広筋・外側広筋・中間広筋)・大臀筋" },
  { id: "ex-deadlift", name: "デッドリフト", category: "背中", barbell: true, equipment: "barbell", pattern: "hinge", muscle: "脊柱起立筋・臀筋・ハムストリングス" },
  { id: "ex-ohp", name: "ショルダープレス", category: "肩", barbell: true, equipment: "barbell", pattern: "push_vertical", muscle: "三角筋前部・中部" },
  { id: "ex-row", name: "バーベルロウ", category: "背中", barbell: true, equipment: "barbell", pattern: "pull_horizontal", muscle: "広背筋・僧帽筋中部・菱形筋" },
  { id: "ex-pullup", name: "懸垂", category: "背中", barbell: false, equipment: "bodyweight", pattern: "pull_vertical", muscle: "広背筋・大円筋" },
  { id: "ex-curl", name: "ダンベルカール", category: "上腕二頭筋", barbell: false, equipment: "dumbbell", pattern: "curl", muscle: "上腕二頭筋(長頭・短頭)" },
  { id: "ex-legpress", name: "レッグプレス", category: "腿前", barbell: false, equipment: "machine", pattern: "squat", muscle: "大腿四頭筋(大腿直筋・内側広筋・外側広筋・中間広筋)・大臀筋" },

  // 胸
  { id: "ex-c-pushup", name: "腕立て伏せ", category: "胸", barbell: false, equipment: "bodyweight", pattern: "push_horizontal", muscle: "大胸筋(胸肋部・中部)" },
  { id: "ex-c-pushup1h", name: "片手腕立て伏せ", category: "胸", barbell: false, equipment: "bodyweight", pattern: "push_horizontal", muscle: "大胸筋(胸肋部・中部)" },
  { id: "ex-c-pushupknee", name: "膝つき腕立て伏せ", category: "胸", barbell: false, equipment: "bodyweight", pattern: "push_horizontal", muscle: "大胸筋(胸肋部・中部)" },
  { id: "ex-c-pushupincline", name: "斜め腕立て伏せ", category: "胸", barbell: false, equipment: "bodyweight", pattern: "push_horizontal", muscle: "大胸筋(胸肋部・中部)" },
  { id: "ex-c-pushupfoot", name: "足上げ腕立て伏せ", category: "胸", barbell: false, equipment: "bodyweight", pattern: "push_horizontal", muscle: "大胸筋(胸肋部・中部)" },
  { id: "ex-c-dips", name: "ディップス", category: "胸", barbell: false, equipment: "bodyweight", pattern: "push_horizontal", muscle: "大胸筋下部・上腕三頭筋" },
  { id: "ex-c-tubepress", name: "チューブチェストプレス", category: "胸", barbell: false, equipment: "band", pattern: "push_horizontal", muscle: "大胸筋(胸肋部・中部)" },
  { id: "ex-c-tubeinclinepress", name: "チューブインクラインプレス", category: "胸", barbell: false, equipment: "band", pattern: "push_horizontal", muscle: "大胸筋上部(鎖骨部)" },
  { id: "ex-c-tubedeclinepress", name: "チューブデクラインプレス", category: "胸", barbell: false, equipment: "band", pattern: "push_horizontal", muscle: "大胸筋下部" },
  { id: "ex-c-tubecrosspress", name: "チューブクロスプレス", category: "胸", barbell: false, equipment: "band", pattern: "push_horizontal", muscle: "大胸筋(胸肋部・中部)" },
  { id: "ex-c-tubefly", name: "チューブチェストフライ", category: "胸", barbell: false, equipment: "band", pattern: "fly", muscle: "大胸筋(内側・ストレッチ域)" },
  { id: "ex-c-tubeinclinefly", name: "チューブインクラインフライ", category: "胸", barbell: false, equipment: "band", pattern: "fly", muscle: "大胸筋上部(鎖骨部)" },
  { id: "ex-c-tubedeclinefly", name: "チューブデクラインフライ", category: "胸", barbell: false, equipment: "band", pattern: "fly", muscle: "大胸筋下部" },
  { id: "ex-c-tubecrossfly", name: "チューブクロスフライ", category: "胸", barbell: false, equipment: "band", pattern: "fly", muscle: "大胸筋(内側・ストレッチ域)" },
  { id: "ex-c-dbpress", name: "ダンベルプレス", category: "胸", barbell: false, equipment: "dumbbell", pattern: "push_horizontal", muscle: "大胸筋(胸肋部・中部)" },
  { id: "ex-c-dbinclinepress", name: "ダンベルインクラインプレス", category: "胸", barbell: false, equipment: "dumbbell", pattern: "push_horizontal", muscle: "大胸筋上部(鎖骨部)" },
  { id: "ex-c-dbdeclinepress", name: "ダンベルデクラインプレス", category: "胸", barbell: false, equipment: "dumbbell", pattern: "push_horizontal", muscle: "大胸筋下部" },
  { id: "ex-c-dbfly", name: "ダンベルフライ", category: "胸", barbell: false, equipment: "dumbbell", pattern: "fly", muscle: "大胸筋(内側・ストレッチ域)" },
  { id: "ex-c-dbinclinefly", name: "ダンベルインクラインフライ", category: "胸", barbell: false, equipment: "dumbbell", pattern: "fly", muscle: "大胸筋上部(鎖骨部)" },
  { id: "ex-c-dbdeclinefly", name: "ダンベルデクラインフライ", category: "胸", barbell: false, equipment: "dumbbell", pattern: "fly", muscle: "大胸筋下部" },
  { id: "ex-c-bbinclinebench", name: "バーベルインクラインベンチプレス", category: "胸", barbell: true, equipment: "barbell", pattern: "push_horizontal", muscle: "大胸筋上部(鎖骨部)" },
  { id: "ex-c-bbdeclinebench", name: "バーベルデクラインベンチプレス", category: "胸", barbell: true, equipment: "barbell", pattern: "push_horizontal", muscle: "大胸筋下部" },
  { id: "ex-c-bbnarrowbench", name: "バーベルナローベンチプレス", category: "胸", barbell: true, equipment: "barbell", pattern: "push_horizontal", muscle: "上腕三頭筋・大胸筋内側" },
  { id: "ex-c-bbwidebench", name: "バーベルワイドベンチプレス", category: "胸", barbell: true, equipment: "barbell", pattern: "push_horizontal", muscle: "大胸筋外側(胸肋部)" },
  { id: "ex-c-bbreversebench", name: "バーベルリバースグリップベンチプレス", category: "胸", barbell: true, equipment: "barbell", pattern: "push_horizontal", muscle: "大胸筋(胸肋部・中部)" },
  { id: "ex-c-machinepress", name: "マシンチェストプレス", category: "胸", barbell: false, equipment: "machine", pattern: "push_horizontal", muscle: "大胸筋(胸肋部・中部)" },
  { id: "ex-c-machinefly", name: "マシンチェストフライ", category: "胸", barbell: false, equipment: "machine", pattern: "fly", muscle: "大胸筋(内側・ストレッチ域)" },
  { id: "ex-c-smithbench", name: "スミスマシンベンチプレス", category: "胸", barbell: true, equipment: "barbell", pattern: "push_horizontal", muscle: "大胸筋(胸肋部・中部)" },
  { id: "ex-c-smithinclinebench", name: "スミスマシンインクラインプレス", category: "胸", barbell: true, equipment: "barbell", pattern: "push_horizontal", muscle: "大胸筋上部(鎖骨部)" },
  { id: "ex-c-smithdeclinebench", name: "スミスマシンデクラインプレス", category: "胸", barbell: true, equipment: "barbell", pattern: "push_horizontal", muscle: "大胸筋下部" },
  { id: "ex-c-cablefly", name: "ケーブルフライ", category: "胸", barbell: false, equipment: "cable", pattern: "fly", muscle: "大胸筋(内側・ストレッチ域)" },
  { id: "ex-c-cablecrossover", name: "ケーブルクロスオーバー", category: "胸", barbell: false, equipment: "cable", pattern: "push_horizontal", muscle: "大胸筋" },

  // 背中
  { id: "ex-b-invertedrow", name: "斜め懸垂", category: "背中", barbell: false, equipment: "bodyweight", pattern: "pull_vertical", muscle: "広背筋・僧帽筋中部" },
  { id: "ex-b-backext", name: "バックエクステンション", category: "背中", barbell: false, equipment: "bodyweight", pattern: "core", muscle: "脊柱起立筋" },
  { id: "ex-b-tubelat", name: "チューブラットプルダウン", category: "背中", barbell: false, equipment: "band", pattern: "pull_vertical", muscle: "広背筋" },
  { id: "ex-b-tuberow", name: "チューブローイング", category: "背中", barbell: false, equipment: "band", pattern: "pull_horizontal", muscle: "広背筋・僧帽筋中部・菱形筋" },
  { id: "ex-b-tubebentrow", name: "チューブベントオーバーロー", category: "背中", barbell: false, equipment: "band", pattern: "pull_horizontal", muscle: "広背筋・僧帽筋中部・菱形筋" },
  { id: "ex-b-tube1hrow", name: "チューブワンハンドローイング", category: "背中", barbell: false, equipment: "band", pattern: "pull_horizontal", muscle: "広背筋・僧帽筋中部・菱形筋" },
  { id: "ex-b-tubereversefly", name: "チューブリバースフライ", category: "背中", barbell: false, equipment: "band", pattern: "pull_horizontal", muscle: "広背筋・僧帽筋・脊柱起立筋" },
  { id: "ex-b-tubeshrug", name: "チューブショルダーシュラッグ", category: "背中", barbell: false, equipment: "band", pattern: "shrug", muscle: "僧帽筋上部" },
  { id: "ex-b-tubebackext", name: "チューブバックエクステンション", category: "背中", barbell: false, equipment: "band", pattern: "core", muscle: "脊柱起立筋" },
  { id: "ex-b-tubegoodmorning", name: "チューブグッドモーニング", category: "背中", barbell: false, equipment: "band", pattern: "hinge", muscle: "脊柱起立筋・ハムストリングス" },
  { id: "ex-b-dbrow", name: "ダンベルローイング", category: "背中", barbell: false, equipment: "dumbbell", pattern: "pull_horizontal", muscle: "広背筋・僧帽筋中部・菱形筋" },
  { id: "ex-b-db1hrow", name: "ワンハンドローイング", category: "背中", barbell: false, equipment: "bodyweight", pattern: "pull_horizontal", muscle: "広背筋・僧帽筋中部・菱形筋" },
  { id: "ex-b-dbshrug", name: "ダンベルショルダーシュラッグ", category: "背中", barbell: false, equipment: "dumbbell", pattern: "shrug", muscle: "僧帽筋上部" },
  { id: "ex-b-dbdeadlift", name: "ダンベルデッドリフト", category: "背中", barbell: false, equipment: "dumbbell", pattern: "hinge", muscle: "脊柱起立筋・臀筋・ハムストリングス" },
  { id: "ex-b-dbpullover", name: "ダンベルプルオーバー", category: "背中", barbell: false, equipment: "dumbbell", pattern: "fly", muscle: "広背筋・大胸筋" },
  { id: "ex-b-dbreversefly", name: "ダンベルリバースフライ", category: "背中", barbell: false, equipment: "dumbbell", pattern: "pull_horizontal", muscle: "広背筋・僧帽筋・脊柱起立筋" },
  { id: "ex-b-dbgoodmorning", name: "ダンベルグッドモーニング", category: "背中", barbell: false, equipment: "dumbbell", pattern: "hinge", muscle: "脊柱起立筋・ハムストリングス" },
  { id: "ex-b-bbpullover", name: "バーベルプルオーバー", category: "背中", barbell: true, equipment: "barbell", pattern: "fly", muscle: "広背筋・大胸筋" },
  { id: "ex-b-bbbentrow", name: "バーベルベントオーバーロー", category: "背中", barbell: true, equipment: "barbell", pattern: "pull_horizontal", muscle: "広背筋・僧帽筋中部・菱形筋" },
  { id: "ex-b-bbshrug", name: "バーベルショルダーシュラッグ", category: "背中", barbell: true, equipment: "barbell", pattern: "shrug", muscle: "僧帽筋上部" },
  { id: "ex-b-bbgoodmorning", name: "バーベルグッドモーニング", category: "背中", barbell: true, equipment: "barbell", pattern: "hinge", muscle: "脊柱起立筋・ハムストリングス" },
  { id: "ex-b-cablelat", name: "ケーブルラットプルダウン", category: "背中", barbell: false, equipment: "cable", pattern: "pull_vertical", muscle: "広背筋" },
  { id: "ex-b-cablerow", name: "ケーブルローイング", category: "背中", barbell: false, equipment: "cable", pattern: "pull_horizontal", muscle: "広背筋・僧帽筋中部・菱形筋" },
  { id: "ex-b-smithdeadlift", name: "スミスマシンデッドリフト", category: "背中", barbell: true, equipment: "barbell", pattern: "hinge", muscle: "脊柱起立筋・臀筋・ハムストリングス" },
  { id: "ex-b-tbarrow", name: "Tバーローイング", category: "背中", barbell: false, equipment: "machine", pattern: "pull_horizontal", muscle: "広背筋・僧帽筋中部・菱形筋" },
  { id: "ex-b-chinup", name: "逆手懸垂", category: "背中", barbell: false, equipment: "bodyweight", pattern: "pull_vertical", muscle: "広背筋・上腕二頭筋" },

  // 肩
  { id: "ex-s-pikepushup", name: "パイクプッシュアップ", category: "肩", barbell: false, equipment: "bodyweight", pattern: "push_vertical", muscle: "三角筋前部・中部" },
  { id: "ex-s-handstandpushup", name: "逆立ち腕立て伏せ", category: "肩", barbell: false, equipment: "bodyweight", pattern: "push_vertical", muscle: "三角筋前部・中部" },
  { id: "ex-s-tubepress", name: "チューブショルダープレス", category: "肩", barbell: false, equipment: "band", pattern: "push_vertical", muscle: "三角筋前部・中部" },
  { id: "ex-s-tubeupright", name: "チューブアップライトロー", category: "肩", barbell: false, equipment: "band", pattern: "raise", muscle: "三角筋中部・僧帽筋上部" },
  { id: "ex-s-tubefront", name: "チューブフロントレイズ", category: "肩", barbell: false, equipment: "band", pattern: "raise", muscle: "三角筋前部" },
  { id: "ex-s-tubeside", name: "チューブサイドレイズ", category: "肩", barbell: false, equipment: "band", pattern: "raise", muscle: "三角筋中部(側部)" },
  { id: "ex-s-tuberear", name: "チューブリアラテラルレイズ", category: "肩", barbell: false, equipment: "band", pattern: "pull_horizontal", muscle: "三角筋後部" },
  { id: "ex-s-tubefacepull", name: "チューブフェイスプル", category: "肩", barbell: false, equipment: "band", pattern: "pull_horizontal", muscle: "三角筋後部・僧帽筋中部" },
  { id: "ex-s-dbpress", name: "ダンベルショルダープレス", category: "肩", barbell: false, equipment: "dumbbell", pattern: "push_vertical", muscle: "三角筋前部・中部" },
  { id: "ex-s-dbupright", name: "ダンベルアップライトロー", category: "肩", barbell: false, equipment: "dumbbell", pattern: "raise", muscle: "三角筋中部・僧帽筋上部" },
  { id: "ex-s-dbside", name: "ダンベルサイドレイズ", category: "肩", barbell: false, equipment: "dumbbell", pattern: "raise", muscle: "三角筋中部(側部)" },
  { id: "ex-s-dbfront", name: "ダンベルフロントレイズ", category: "肩", barbell: false, equipment: "dumbbell", pattern: "raise", muscle: "三角筋前部" },
  { id: "ex-s-dbrear", name: "ダンベルリアラテラルレイズ", category: "肩", barbell: false, equipment: "dumbbell", pattern: "pull_horizontal", muscle: "三角筋後部" },
  { id: "ex-s-dbfacepull", name: "ダンベルフェイスプル", category: "肩", barbell: false, equipment: "dumbbell", pattern: "pull_horizontal", muscle: "三角筋後部・僧帽筋中部" },
  { id: "ex-s-bbupright", name: "バーベルアップライトロウ", category: "肩", barbell: true, equipment: "barbell", pattern: "pull_horizontal", muscle: "三角筋" },
  { id: "ex-s-machinepress", name: "マシンショルダープレス", category: "肩", barbell: false, equipment: "machine", pattern: "push_vertical", muscle: "三角筋前部・中部" },
  { id: "ex-s-smithpress", name: "スミスマシンショルダープレス", category: "肩", barbell: true, equipment: "barbell", pattern: "push_vertical", muscle: "三角筋前部・中部" },
  { id: "ex-s-cabledelta", name: "ケーブルデルタレイズ", category: "肩", barbell: false, equipment: "cable", pattern: "raise", muscle: "三角筋中部(側部)" },

  // 腕
  { id: "ex-a-diamondpushup", name: "ダイヤモンド腕立て伏せ", category: "上腕三頭筋", barbell: false, equipment: "bodyweight", pattern: "push_horizontal", muscle: "上腕三頭筋・大胸筋内側" },
  { id: "ex-a-narrowpushup", name: "ナロープッシュアップ", category: "上腕三頭筋", barbell: false, equipment: "cable", pattern: "push_horizontal", muscle: "上腕三頭筋・大胸筋内側" },
  { id: "ex-a-tubepressdown", name: "チューブプレスダウン", category: "上腕三頭筋", barbell: false, equipment: "band", pattern: "extension", muscle: "上腕三頭筋外側頭・内側頭" },
  { id: "ex-a-tubefrench", name: "チューブフレンチプレス", category: "上腕三頭筋", barbell: false, equipment: "band", pattern: "extension", muscle: "上腕三頭筋長頭" },
  { id: "ex-a-tubetricepsext", name: "チューブトライセプスエクステンション", category: "上腕三頭筋", barbell: false, equipment: "band", pattern: "extension", muscle: "上腕三頭筋(全体)" },
  { id: "ex-a-tubekickback", name: "チューブキックバック", category: "上腕三頭筋", barbell: false, equipment: "band", pattern: "extension", muscle: "上腕三頭筋外側頭" },
  { id: "ex-a-dbtricepspress", name: "ダンベルトライセプスプレス", category: "上腕三頭筋", barbell: false, equipment: "dumbbell", pattern: "push_horizontal", muscle: "上腕三頭筋(全体)" },
  { id: "ex-a-dbfrench", name: "ダンベルフレンチプレス", category: "上腕三頭筋", barbell: false, equipment: "dumbbell", pattern: "extension", muscle: "上腕三頭筋長頭" },
  { id: "ex-a-dbkickback", name: "ダンベルキックバック", category: "上腕三頭筋", barbell: false, equipment: "dumbbell", pattern: "extension", muscle: "上腕三頭筋外側頭" },
  { id: "ex-a-dbtate", name: "ダンベルテイトプレス", category: "上腕三頭筋", barbell: false, equipment: "dumbbell", pattern: "push_horizontal", muscle: "上腕三頭筋(全体)" },
  { id: "ex-a-bbfrench", name: "バーベルフレンチプレス", category: "上腕三頭筋", barbell: true, equipment: "barbell", pattern: "extension", muscle: "上腕三頭筋長頭" },
  { id: "ex-a-cablepressdown", name: "ケーブルプレスダウン", category: "上腕三頭筋", barbell: false, equipment: "cable", pattern: "extension", muscle: "上腕三頭筋外側頭・内側頭" },
  { id: "ex-a-ropepressdown", name: "ローププレスダウン", category: "上腕三頭筋", barbell: false, equipment: "cable", pattern: "extension", muscle: "上腕三頭筋外側頭・内側頭" },
  { id: "ex-a-tubecurl", name: "チューブカール", category: "上腕二頭筋", barbell: false, equipment: "band", pattern: "curl", muscle: "上腕二頭筋(長頭・短頭)" },
  { id: "ex-a-tubehammercurl", name: "チューブハンマーカール", category: "上腕二頭筋", barbell: false, equipment: "band", pattern: "curl", muscle: "上腕二頭筋(長頭)・腕橈骨筋" },
  { id: "ex-a-tubedragcurl", name: "チューブドラッグカール", category: "上腕二頭筋", barbell: false, equipment: "band", pattern: "curl", muscle: "上腕二頭筋短頭" },
  { id: "ex-a-tubeconccurl", name: "チューブコンセントレーションカール", category: "上腕二頭筋", barbell: false, equipment: "band", pattern: "curl", muscle: "上腕二頭筋短頭" },
  { id: "ex-a-tubereversecurl", name: "チューブリバースカール", category: "前腕", barbell: false, equipment: "band", pattern: "curl", muscle: "腕橈骨筋・上腕筋" },
  { id: "ex-a-dbconccurl", name: "ダンベルコンセントレーションカール", category: "上腕二頭筋", barbell: false, equipment: "dumbbell", pattern: "curl", muscle: "上腕二頭筋短頭" },
  { id: "ex-a-dbhammercurl", name: "ダンベルハンマーカール", category: "上腕二頭筋", barbell: false, equipment: "dumbbell", pattern: "curl", muscle: "上腕二頭筋(長頭)・腕橈骨筋" },
  { id: "ex-a-dbsidecurl", name: "ダンベルサイドカール", category: "上腕二頭筋", barbell: false, equipment: "dumbbell", pattern: "curl", muscle: "上腕二頭筋・腕橈骨筋" },
  { id: "ex-a-dbinclinecurl", name: "ダンベルインクラインカール", category: "上腕二頭筋", barbell: false, equipment: "dumbbell", pattern: "curl", muscle: "上腕二頭筋長頭(ストレッチ位置)" },
  { id: "ex-a-bbcurl", name: "バーベルカール", category: "上腕二頭筋", barbell: true, equipment: "barbell", pattern: "curl", muscle: "上腕二頭筋(長頭・短頭)" },
  { id: "ex-a-bbreversecurl", name: "バーベルリバースカール", category: "前腕", barbell: true, equipment: "barbell", pattern: "curl", muscle: "腕橈骨筋・上腕筋" },
  { id: "ex-a-bbdragcurl", name: "バーベルドラッグカール", category: "上腕二頭筋", barbell: true, equipment: "barbell", pattern: "curl", muscle: "上腕二頭筋短頭" },
  { id: "ex-a-cablecurl", name: "ケーブルカール", category: "上腕二頭筋", barbell: false, equipment: "cable", pattern: "curl", muscle: "上腕二頭筋(長頭・短頭)" },

  // 脚
  { id: "ex-l-bulgariansquat", name: "ブルガリアンスクワット", category: "腿前", barbell: false, equipment: "bodyweight", pattern: "lunge", muscle: "大腿四頭筋・大臀筋" },
  { id: "ex-l-sissysquat", name: "シシースクワット", category: "腿前", barbell: false, equipment: "bodyweight", pattern: "squat", muscle: "大腿四頭筋(大腿直筋中心)" },
  { id: "ex-l-sidelunge", name: "サイドランジ", category: "腿前", barbell: false, equipment: "bodyweight", pattern: "lunge", muscle: "大腿四頭筋・大臀筋" },
  { id: "ex-l-tubesquat", name: "チューブスクワット", category: "腿前", barbell: false, equipment: "band", pattern: "squat", muscle: "大腿四頭筋(大腿直筋・内側広筋・外側広筋・中間広筋)・大臀筋" },
  { id: "ex-l-tubeleglift", name: "チューブレッグリフト", category: "腿前", barbell: false, equipment: "band", pattern: "squat", muscle: "大腿四頭筋・ハムストリングス・臀筋" },
  { id: "ex-l-tubelegext", name: "チューブレッグエクステンション", category: "腿前", barbell: false, equipment: "band", pattern: "extension", muscle: "大腿四頭筋(大腿直筋中心)" },
  { id: "ex-l-tubelegcurl", name: "チューブレッグカール", category: "腿裏", barbell: false, equipment: "band", pattern: "curl", muscle: "ハムストリングス" },
  { id: "ex-l-dbsquat", name: "ダンベルスクワット", category: "腿前", barbell: false, equipment: "dumbbell", pattern: "squat", muscle: "大腿四頭筋(大腿直筋・内側広筋・外側広筋・中間広筋)・大臀筋" },
  { id: "ex-l-dblunge", name: "ダンベルランジ", category: "腿前", barbell: false, equipment: "dumbbell", pattern: "lunge", muscle: "大腿四頭筋・大臀筋" },
  { id: "ex-l-dbsidelunge", name: "ダンベルサイドランジ", category: "腿前", barbell: false, equipment: "dumbbell", pattern: "lunge", muscle: "大腿四頭筋・大臀筋" },
  { id: "ex-l-dblegext", name: "ダンベルレッグエクステンション", category: "腿前", barbell: false, equipment: "dumbbell", pattern: "extension", muscle: "大腿四頭筋(大腿直筋中心)" },
  { id: "ex-l-dblegcurl", name: "ダンベルレッグカール", category: "腿裏", barbell: false, equipment: "dumbbell", pattern: "curl", muscle: "ハムストリングス" },
  { id: "ex-l-dbstiffdeadlift", name: "ダンベルスティッフレッグドデッドリフト", category: "腿裏", barbell: false, equipment: "dumbbell", pattern: "hinge", muscle: "ハムストリングス・臀筋" },
  { id: "ex-l-bbfrontsquat", name: "バーベルフロントスクワット", category: "腿前", barbell: true, equipment: "barbell", pattern: "squat", muscle: "大腿四頭筋中心" },
  { id: "ex-l-bbfrontlunge", name: "バーベルフロントランジ", category: "腿前", barbell: true, equipment: "barbell", pattern: "lunge", muscle: "大腿四頭筋中心" },
  { id: "ex-l-bbsidelunge", name: "バーベルサイドランジ", category: "腿前", barbell: true, equipment: "barbell", pattern: "lunge", muscle: "大腿四頭筋・大臀筋" },
  { id: "ex-l-bbstiffdeadlift", name: "バーベルスティッフレッグドデッドリフト", category: "腿裏", barbell: true, equipment: "barbell", pattern: "hinge", muscle: "ハムストリングス・臀筋" },
  { id: "ex-l-smithsquat", name: "スミスマシンスクワット", category: "腿前", barbell: true, equipment: "barbell", pattern: "squat", muscle: "大腿四頭筋(大腿直筋・内側広筋・外側広筋・中間広筋)・大臀筋" },
  { id: "ex-l-hacksquat", name: "ハックスクワット", category: "腿前", barbell: false, equipment: "machine", pattern: "squat", muscle: "大腿四頭筋(大腿直筋・内側広筋・外側広筋・中間広筋)・大臀筋" },
  { id: "ex-l-machinelegext", name: "マシンレッグエクステンション", category: "腿前", barbell: false, equipment: "machine", pattern: "extension", muscle: "大腿四頭筋(大腿直筋中心)" },
  { id: "ex-l-machinelegcurl", name: "マシンレッグカール", category: "腿裏", barbell: false, equipment: "machine", pattern: "curl", muscle: "ハムストリングス" },
  { id: "ex-l-machineadduction", name: "マシンアダクション", category: "腿前", barbell: false, equipment: "machine", pattern: "squat", muscle: "内転筋群" },
  { id: "ex-l-dbcalfraise", name: "ダンベルカーフレイズ", category: "ふくらはぎ", barbell: false, equipment: "dumbbell", pattern: "calf", muscle: "腓腹筋・ヒラメ筋" },

  // その他(腹筋・体幹)
  { id: "ex-o-crunch", name: "クランチ", category: "腹筋", barbell: false, equipment: "bodyweight", pattern: "core", muscle: "腹直筋上部" },
  { id: "ex-o-legraise", name: "レッグレイズ", category: "腹筋", barbell: false, equipment: "bodyweight", pattern: "core", muscle: "腹直筋下部・腸腰筋" },
  { id: "ex-o-reversecrunch", name: "リバースクランチ", category: "腹筋", barbell: false, equipment: "bodyweight", pattern: "core", muscle: "腹直筋下部" },
  { id: "ex-o-figure4crunch", name: "四の字クランチ", category: "腹筋", barbell: false, equipment: "bodyweight", pattern: "core", muscle: "腹直筋上部" },
  { id: "ex-o-curlup", name: "カールアップ", category: "腹筋", barbell: false, equipment: "bodyweight", pattern: "core", muscle: "腹直筋上部" },
  { id: "ex-o-tubecrunch", name: "チューブクランチ", category: "腹筋", barbell: false, equipment: "band", pattern: "core", muscle: "腹直筋上部" },
  { id: "ex-o-tubelegraise", name: "チューブレッグレイズ", category: "腹筋", barbell: false, equipment: "band", pattern: "core", muscle: "腹直筋下部・腸腰筋" },
  { id: "ex-o-tubeabstwist", name: "チューブアブツイスト", category: "腹筋", barbell: false, equipment: "band", pattern: "core", muscle: "腹斜筋" },
  { id: "ex-o-tubesidebend", name: "チューブサイドベント", category: "腹筋", barbell: false, equipment: "band", pattern: "core", muscle: "腹斜筋" },
  { id: "ex-o-dbcrunch", name: "ダンベルクランチ", category: "腹筋", barbell: false, equipment: "dumbbell", pattern: "core", muscle: "腹直筋上部" },
  { id: "ex-o-dbsidebend", name: "ダンベルサイドベント", category: "腹筋", barbell: false, equipment: "dumbbell", pattern: "core", muscle: "腹斜筋" },
  { id: "ex-o-dblegraise", name: "ダンベルレッグレイズ", category: "腹筋", barbell: false, equipment: "dumbbell", pattern: "core", muscle: "腹直筋下部・腸腰筋" },
  { id: "ex-o-dbtoetouch", name: "ダンベルトゥタッチクランチ", category: "腹筋", barbell: false, equipment: "dumbbell", pattern: "core", muscle: "腹直筋上部" },
  { id: "ex-o-dbtwist", name: "ダンベルツイスト", category: "腹筋", barbell: false, equipment: "dumbbell", pattern: "core", muscle: "腹斜筋" },
  { id: "ex-o-dbrussiantwist", name: "ダンベルロシアンツイスト", category: "腹筋", barbell: false, equipment: "dumbbell", pattern: "core", muscle: "腹斜筋" },
  { id: "ex-o-cablecrunch", name: "ケーブルクランチ", category: "腹筋", barbell: false, equipment: "cable", pattern: "core", muscle: "腹直筋上部" },
  { id: "ex-o-frontplank", name: "フロントプランク", category: "腹筋", barbell: false, equipment: "bodyweight", pattern: "core", muscle: "腹直筋・体幹深層筋" },
  { id: "ex-o-machinecrunch", name: "マシンクランチ", category: "腹筋", barbell: false, equipment: "machine", pattern: "core", muscle: "腹直筋上部" },

  // 追加分(部位ごとの器具バリエーション補強)
  { id: "ex-c-widepushup", name: "ワイドプッシュアップ", category: "胸", barbell: false, equipment: "bodyweight", pattern: "push_horizontal", muscle: "大胸筋外側(胸肋部)" },
  { id: "ex-c-cablechestpress", name: "ケーブルチェストプレス", category: "胸", barbell: false, equipment: "cable", pattern: "push_horizontal", muscle: "大胸筋(胸肋部・中部)" },

  { id: "ex-b-machinerow", name: "マシンローイング", category: "背中", barbell: false, equipment: "machine", pattern: "pull_horizontal", muscle: "広背筋・僧帽筋中部・菱形筋" },
  { id: "ex-b-machinepulldown", name: "マシンプルダウン", category: "背中", barbell: false, equipment: "machine", pattern: "pull_vertical", muscle: "広背筋" },
  { id: "ex-b-cablepullover", name: "ケーブルプルオーバー", category: "背中", barbell: false, equipment: "cable", pattern: "fly", muscle: "広背筋・大胸筋" },

  { id: "ex-s-cableupright", name: "ケーブルアップライトロー", category: "肩", barbell: false, equipment: "cable", pattern: "raise", muscle: "三角筋中部・僧帽筋上部" },
  { id: "ex-s-cablefront", name: "ケーブルフロントレイズ", category: "肩", barbell: false, equipment: "cable", pattern: "raise", muscle: "三角筋前部" },
  { id: "ex-s-declinepike", name: "デクラインパイクプッシュアップ", category: "肩", barbell: false, equipment: "bodyweight", pattern: "push_vertical", muscle: "三角筋前部・中部" },
  { id: "ex-s-bbbehindneck", name: "バーベルビハインドネックプレス", category: "肩", barbell: true, equipment: "barbell", pattern: "push_vertical", muscle: "三角筋中部・後部" },

  { id: "ex-a-machinecurl", name: "マシンカール", category: "上腕二頭筋", barbell: false, equipment: "machine", pattern: "curl", muscle: "上腕二頭筋(長頭・短頭)" },
  { id: "ex-a-machinetriceps", name: "マシントライセプスエクステンション", category: "上腕三頭筋", barbell: false, equipment: "machine", pattern: "extension", muscle: "上腕三頭筋(全体)" },
  { id: "ex-a-benchdips", name: "ベンチディップス", category: "上腕三頭筋", barbell: false, equipment: "bodyweight", pattern: "push_horizontal", muscle: "大胸筋下部・上腕三頭筋" },
  { id: "ex-a-reversepushup", name: "リバースプッシュアップ", category: "上腕三頭筋", barbell: false, equipment: "bodyweight", pattern: "push_horizontal", muscle: "上腕三頭筋・大胸筋下部" },

  { id: "ex-l-cablekickback", name: "ケーブルキックバック", category: "臀部", barbell: false, equipment: "cable", pattern: "extension", muscle: "大臀筋" },
  { id: "ex-l-cablehipthrust", name: "ケーブルヒップスラスト", category: "臀部", barbell: false, equipment: "cable", pattern: "squat", muscle: "大臀筋" },
  { id: "ex-l-glutebridge", name: "グルートブリッジ", category: "臀部", barbell: false, equipment: "bodyweight", pattern: "squat", muscle: "大臀筋" },
  { id: "ex-l-cablelegcurl", name: "ケーブルレッグカール", category: "腿裏", barbell: false, equipment: "cable", pattern: "curl", muscle: "ハムストリングス" },
  { id: "ex-l-bwsquat", name: "自重スクワット", category: "腿前", barbell: false, equipment: "bodyweight", pattern: "squat", muscle: "大腿四頭筋(大腿直筋・内側広筋・外側広筋・中間広筋)・大臀筋" },
  { id: "ex-l-bwlunge", name: "自重ランジ", category: "腿前", barbell: false, equipment: "bodyweight", pattern: "lunge", muscle: "大腿四頭筋・大臀筋" },
  { id: "ex-l-calfraise", name: "カーフレイズ", category: "ふくらはぎ", barbell: false, equipment: "bodyweight", pattern: "calf", muscle: "腓腹筋・ヒラメ筋" },

  { id: "ex-o-bbrollout", name: "バーベルロールアウト", category: "腹筋", barbell: true, equipment: "barbell", pattern: "core", muscle: "腹直筋・体幹深層筋" },
  { id: "ex-o-abwheel", name: "腹筋ローラー", category: "腹筋", barbell: false, equipment: "bodyweight", pattern: "core", muscle: "腹直筋・体幹深層筋" },
  { id: "ex-o-cablewoodchop", name: "ケーブルウッドチョッパー", category: "腹筋", barbell: false, equipment: "cable", pattern: "core", muscle: "腹斜筋" },
  { id: "ex-o-hanginglegraise", name: "ハンギングレッグレイズ", category: "腹筋", barbell: false, equipment: "bodyweight", pattern: "core", muscle: "腹直筋下部・腸腰筋" },
  { id: "ex-o-sideplank", name: "サイドプランク", category: "腹筋", barbell: false, equipment: "bodyweight", pattern: "core", muscle: "腹斜筋・体幹深層筋" },
];

const STORAGE_KEY = "overload-data";

const CATEGORIES = ["胸", "背中", "肩", "腿前", "腿裏", "臀部", "ふくらはぎ", "上腕二頭筋", "上腕三頭筋", "前腕", "腹筋"];

const EQUIPMENT_ORDER = ["barbell", "dumbbell", "cable", "bodyweight", "machine", "band"];
const EQUIPMENT_LABELS = {
  barbell: "バーベル種目",
  dumbbell: "ダンベル種目",
  cable: "ケーブル種目",
  bodyweight: "自重種目",
  machine: "マシン種目",
  band: "チューブ種目",
};

// 消費カロリーの概算。METs(運動強度の指標)を使った一般的な計算式:
// 消費カロリー(kcal) = MET × 3.5 × 体重(kg) / 200 × 運動時間(分)
// ウェイトトレーニング(挙上中、一般的な強度)は MET 5.0、セット間の休憩中は MET 2.0(立って休む程度)を採用。
// 1レップあたりの挙上時間は目安として3秒と仮定して算出(あくまで概算値)。
const WEIGHT_TRAINING_MET = 5.0;
const REST_MET = 2.0;
const SECONDS_PER_REP = 3;
function caloriesForMinutes(met, bodyWeightKg, minutes) {
  if (!bodyWeightKg || minutes <= 0) return 0;
  return ((met * 3.5 * bodyWeightKg) / 200) * minutes;
}
function estimateSetCalories(reps, bodyWeightKg) {
  if (!bodyWeightKg || !reps) return 0;
  const minutes = (reps * SECONDS_PER_REP) / 60;
  return caloriesForMinutes(WEIGHT_TRAINING_MET, bodyWeightKg, minutes);
}

function beep() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.frequency.value = 880;
    gain.gain.setValueAtTime(0.15, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.5);
    osc.start();
    osc.stop(ctx.currentTime + 0.5);
  } catch (e) {}
}

function vibrate() {
  try {
    if (navigator.vibrate) navigator.vibrate([200, 100, 200]);
  } catch (e) {}
}

function fmtDate(iso) {
  const d = new Date(iso);
  return `${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()} (${"日月火水木金土"[d.getDay()]})`;
}

const CATEGORY_MUSCLE = {
  "胸": "大胸筋",
  "背中": "広背筋・僧帽筋・脊柱起立筋",
  "肩": "三角筋",
  "腕": "上腕二頭筋・上腕三頭筋",
  "脚": "大腿四頭筋・ハムストリングス・臀筋",
  "腿前": "大腿四頭筋",
  "腿裏": "ハムストリングス",
  "臀部": "大臀筋",
  "ふくらはぎ": "腓腹筋・ヒラメ筋",
  "上腕二頭筋": "上腕二頭筋(長頭・短頭)",
  "上腕三頭筋": "上腕三頭筋(長頭・外側頭・内側頭)",
  "前腕": "腕橈骨筋・前腕屈筋群",
  "腹筋": "腹直筋・体幹",
};

const PATTERN_INFO = {
  push_horizontal: {
    how: "ベンチや床に仰向け(または腕立ての姿勢)になり、胸の前でバーやダンベルを構えます。肘を曲げて重量を胸に近づけたあと、腕を伸ばしながら真上または前方へ押し上げます。",
    tips: ["肩をすくめず、肩甲骨を寄せて土台を安定させる", "肘を伸ばしきる直前で力を抜きすぎない", "手首はまっすぐキープする"],
  },
  push_vertical: {
    how: "立った状態(またはしゃがんだ状態)で、バーやダンベルを肩の高さに構えます。体幹を固めたまま、腕をまっすぐ頭上に押し上げます。",
    tips: ["反り腰にならないよう腹圧を入れる", "肘を完全にロックしすぎない", "下ろすときは肩の高さまでコントロールする"],
  },
  pull_vertical: {
    how: "バーにぶら下がる、またはケーブルバーを頭上から握ります。肩甲骨を下げながら、肘を体側に引きつけるようにバーを胸(または顎)の高さまで引き寄せます。",
    tips: ["反動を使わず肩甲骨から動かす", "胸を張って肘を下に引く意識を持つ", "戻すときは腕を完全に伸ばしきる"],
  },
  pull_horizontal: {
    how: "上体を前傾させるか座った姿勢で、バーやハンドルを構えます。肘を後ろに引きながら、肩甲骨を寄せるように体の方へ引き寄せます。",
    tips: ["肩をすくめず、肘から引く意識を持つ", "反動をつけず一定のスピードで引く", "戻すときは肩甲骨を開ききる手前で止める"],
  },
  squat: {
    how: "肩幅程度に足を開いて立ち、バーを背負う(または胸の前に構える)。膝とつま先の向きを揃えながら、股関節と膝を曲げてしゃがみ、立ち上がります。",
    tips: ["膝がつま先より内側に入らないようにする", "背中は丸めずまっすぐを保つ", "かかとに体重を残す意識を持つ"],
  },
  hinge: {
    how: "足を腰幅に開いて立ち、バーを構えます。背中をまっすぐに保ったまま股関節を後ろに引くように上体を前傾させ、股関節を伸ばして元の姿勢に戻ります。",
    tips: ["膝は軽く曲げる程度で、主に股関節を使う", "バーは体に沿わせるように動かす", "腰を反らせすぎない・丸めすぎない"],
  },
  lunge: {
    how: "片足を大きく前(または横)に踏み出し、両膝を曲げて腰を落とします。前脚で床を押すようにして元の姿勢に戻ります。",
    tips: ["前膝がつま先より大きく前に出過ぎないようにする", "上体はまっすぐに保つ", "戻る動作もコントロールして行う"],
  },
  curl: {
    how: "腕を体側に下ろした状態からスタートし、肘の位置を固定したまま前腕を曲げて重量を持ち上げます。ゆっくり戻します。",
    tips: ["肘を前後に振らず固定する", "反動(チーティング)を使いすぎない", "戻すときも力を抜かずコントロールする"],
  },
  extension: {
    how: "対象の関節(肘や膝)を曲げた状態からスタートし、その関節をまっすぐ伸ばすように力を加えます。ゆっくり曲げた位置に戻します。",
    tips: ["伸ばしきったところで一瞬キープすると効果的", "反動を使わない", "可動域を大きく使う"],
  },
  fly: {
    how: "腕を軽く曲げた状態で体の横(または上)に開き、弧を描くように腕を体の前で閉じます。",
    tips: ["肘の角度をキープしたまま動かす(曲げ伸ばししない)", "重すぎる重量だと関節に負担がかかるので注意", "対象の筋肉で挟み込むイメージを持つ"],
  },
  raise: {
    how: "腕を体側に下ろした状態から、肘を軽く曲げたまま(またはまっすぐのまま)腕を横または前に持ち上げ、肩の高さ付近まで上げます。",
    tips: ["勢いをつけず、狙った筋肉でコントロールして上げる", "肩がすくまないようにする", "軽めの重量でフォームを優先する"],
  },
  core: {
    how: "床や台の上で体幹を丸める、または固定する動作を行います。反動を使わず、腹筋の収縮を意識して動かします。",
    tips: ["首に力を入れすぎない(手を添える程度)", "呼吸を止めない", "反動を使わずゆっくり行う"],
  },
  shrug: {
    how: "バーやダンベルを持って立ち、腕を伸ばしたまま肩をすくめるように真上へ引き上げ、ゆっくり戻します。",
    tips: ["肘を曲げて引き上げない(僧帽筋を使う)", "肩を回さず、まっすぐ上下させる", "上げた位置で一瞬キープすると効果的"],
  },
  calf: {
    how: "立った状態(または座った状態)で、かかとを持ち上げてつま先立ちになり、ゆっくり戻します。",
    tips: ["可動域を大きく使う(かかとをしっかり下げる)", "反動を使わずコントロールする", "つま先の向きで効き方が変わる"],
  },
};

// 握り幅・角度・利き手・使用器具などの違いによる追加の説明/コツ。
// 名前に含まれるキーワードに応じて、共通パターンの説明文に上乗せする。
const NAME_MODIFIERS = [
  { test: (n) => n.includes("インクライン") && !n.includes("リバース"), howAdd: "ベンチの角度を上げた姿勢で行うため、通常のフラットな種目より上部に効かせやすくなります。" },
  { test: (n) => n.includes("デクライン"), howAdd: "頭側が下がるようにベンチを傾けて行うため、通常のフラットな種目より下部に効かせやすくなります。" },
  { test: (n) => n.includes("ナロー"), howAdd: "肩幅よりやや狭いグリップ(またはスタンス)で行うため、可動域が深くなり負荷が集中しやすくなります。", tipAdd: "手幅・足幅を狭くしすぎると手首や膝に負担がかかるので注意する" },
  { test: (n) => n.includes("ワイド"), howAdd: "肩幅より広いグリップ(またはスタンス)で行うため、可動域はやや狭くなりますが、負荷のかかる範囲が広がります。", tipAdd: "肩や股関節に負担がかかりやすいので、無理のない可動域で行う" },
  { test: (n) => n.includes("リバースグリップ") || n.includes("リバースカール"), howAdd: "逆手(手のひらが自分側、または上を向く向き)で握って行うため、通常の握り方と負荷のかかり方が変わります。", tipAdd: "手首を痛めやすいので、軽めの重量から試して感覚を確認する" },
  { test: (n) => n.includes("片手") || n.includes("ワンハンド") || n.includes("サイド") && !n.includes("サイドレイズ") && !n.includes("サイドベント"), howAdd: "片側ずつ交互に行うため、左右の筋力差の改善や体幹の安定性向上にもつながります。", tipAdd: "体が横に倒れたり回転したりしないよう、体幹をしっかり固定する" },
  { test: (n) => n.includes("スミスマシン"), howAdd: "軌道が固定されたスミスマシンを使うため、バランスを取る負担が少なく、狙った重量に集中しやすい種目です。", tipAdd: "軌道が固定されている分、フリーウェイトと肩・膝への負荷のかかり方が変わることを意識する" },
  { test: (n) => n.startsWith("チューブ"), howAdd: "チューブを使うため、伸ばしきる位置に近づくほど負荷が強くなるのが特徴です。", tipAdd: "チューブの張りが緩む動作の始めでも、意識的に力を抜かない" },
  { test: (n) => n.startsWith("ケーブル"), howAdd: "ケーブルを使うため、動作の開始から終わりまで負荷が途切れにくいのが特徴です。", tipAdd: "ケーブルを引く方向と体の向きを意識してブレを抑える" },
  { test: (n) => n.startsWith("マシン"), howAdd: "マシンの軌道に沿って動くため、フォームが安定しやすく狙った部位に効かせやすい種目です。", tipAdd: "シートやパッドの位置を自分の関節に合わせて調整してから行う" },
  { test: (n) => n.includes("バーベル") && !n.includes("スミス"), howAdd: "バーベルを両手で握って行うため、左右均等に高重量を扱いやすいのが特徴です。", tipAdd: "左右の力の入り方に差が出ていないか、バーの傾きで確認する" },
  { test: (n) => n.includes("ダンベル"), howAdd: "ダンベルを使うため、可動域を広く使いやすく、左右の筋力差にも気づきやすい種目です。", tipAdd: "左右の重量・軌道が揃うよう、鏡などでフォームを確認する" },
];

// トレーニングベンチの「角度」そのものが動作に関わる種目だけを対象に、目安の角度を提示する。
// (例:ブルガリアンスクワットのように、ベンチを台としてしか使わない種目は対象外)
const FLAT_BENCH_EXERCISES = new Set([
  "ベンチプレス", "バーベルナローベンチプレス", "バーベルワイドベンチプレス", "バーベルリバースグリップベンチプレス",
  "スミスマシンベンチプレス", "ダンベルプレス", "ダンベルフライ",
  "チューブチェストプレス", "チューブチェストフライ", "チューブクロスプレス", "チューブクロスフライ",
]);
function getBenchAngleTip(name) {
  const isIncline = name.includes("インクライン") && !name.includes("リバース");
  const isDecline = name.includes("デクライン");
  const isPressOrFlyOrCurl = name.includes("プレス") || name.includes("フライ") || name.includes("カール");
  if (isIncline && isPressOrFlyOrCurl) return "ベンチの角度は30〜45度を目安にする(上げすぎると肩の関与が増える)";
  if (isDecline && isPressOrFlyOrCurl) return "ベンチの角度は15〜30度(頭側が下がる向き)を目安にする";
  if (FLAT_BENCH_EXERCISES.has(name)) return "ベンチの角度は0度(水平)が基本";
  return null;
}


const EXERCISE_OVERRIDES = {
  "ブルガリアンスクワット": {
    how: "後ろ足の甲をベンチや台に乗せ、前足一本で体を支えながら腰を落とし、前足で床を押して立ち上がります。",
    tips: ["前足のかかとに体重を残す", "上体を軽く前傾させてバランスを取る", "後ろ足で無理に蹴らず、あくまで補助として使う"],
  },
  "シシースクワット": {
    how: "つま先立ちに近い状態で、膝を前に大きく突き出しながら上体を後ろに倒し、太もも前面を伸ばすように腰を落とします。",
    tips: ["膝を前に出す分、支えが必要なら何かに掴まって行う", "反動を使わずゆっくり下ろす", "腰ではなく膝の曲げ伸ばしで動作する"],
  },
  "フェイスプル": {
    how: "ケーブルやチューブを顔の高さに設定し、肘を左右に大きく開きながら顔に向かって引き寄せます。",
    tips: ["肘を肩より高い位置まで開く", "引き終わりで肩甲骨をしっかり寄せる", "重すぎる重量より、正しい軌道を優先する"],
  },
  "バーベルアップライトロウ": {
    how: "バーを肩幅程度で握り、体の前でまっすぐ真上に近い軌道で肘を高く引き上げます。",
    tips: ["肘を手より高い位置まで引き上げる", "肩に痛みが出る場合は挙げる高さを控えめにする", "バーを体から離しすぎない"],
  },
  "Tバーローイング": {
    how: "バーの片端を固定し、もう片端に体を覆いかぶせるように前傾して、ハンドルを胸に向かって引き寄せます。",
    tips: ["背中が丸まらないよう胸を張る", "肘を体側に沿わせるように引く", "引き終わりで肩甲骨を寄せて一瞬キープする"],
  },
  "ハックスクワット": {
    how: "専用マシンの傾斜したパッドに背中を預け、肩と腰を固定した状態で膝を曲げ伸ばしします。",
    tips: ["足の位置をやや前に置くと安定しやすい", "膝がつま先より内側に入らないようにする", "可動域を深くしすぎて腰が浮かないよう注意する"],
  },
  "ヒップスラスト": {
    how: "肩甲骨をベンチに乗せて座り、バーやケーブルを腰の上に構え、お尻を締めながら腰を高く突き上げます。",
    tips: ["一番上でお尻をしっかり締めて一瞬キープする", "腰を反りすぎず、お尻の力で持ち上げる", "あごを軽く引いて首の力みを抜く"],
  },
  "ケーブルヒップスラスト": {
    how: "四つ這いやスタンディングの姿勢からケーブルを足に引っ掛け、股関節を伸ばすように後方・上方へ蹴り出します。",
    tips: ["反動を使わずお尻の収縮を意識する", "腰を反らしすぎない", "上げきったところで一瞬キープする"],
  },
  "グルートブリッジ": {
    how: "仰向けに寝て膝を立て、お尻を締めながら腰を天井に向かって持ち上げ、ゆっくり下ろします。",
    tips: ["一番上でお尻をしっかり締めて一瞬キープする", "腰を反りすぎず、お尻の力で持ち上げる", "肩から膝までが一直線になる高さを目安にする"],
  },
  "フロントプランク": {
    how: "前腕とつま先で体を支え、頭からかかとまでを一直線に保ったまま姿勢をキープします。",
    tips: ["腰が反ったり落ちたりしないようお腹に力を入れ続ける", "呼吸を止めずに自然に続ける", "肩の真下に肘がくる位置をキープする"],
  },
  "サイドプランク": {
    how: "横向きに寝て片方の前腕とつま先で体を支え、体を一直線に保ったまま姿勢をキープします。",
    tips: ["腰が落ちないよう体幹側面をしっかり締める", "肩の真下に肘がくる位置をキープする", "反対側も同じ時間行いバランスを取る"],
  },
  "バーベルロールアウト": {
    how: "バーベルを両手で持ち膝立ちになり、体をまっすぐ保ったまま前方へ転がし、腹筋の力で元の位置まで引き戻します。",
    tips: ["腰が反らないよう常にお腹に力を入れる", "無理のない範囲まで転がす", "戻すときも反動を使わずコントロールする"],
  },
  "腹筋ローラー": {
    how: "アブローラー(腹筋ローラー)を両手で持ち膝立ち(または立位)になり、体をまっすぐ保ったまま前方へ転がし、腹筋の力で元の位置まで引き戻します。",
    tips: ["腰が反らないよう常にお腹に力を入れ続ける", "最初は膝立ちで無理のない範囲から始める", "戻すときも反動を使わず、腹筋でコントロールする", "肩の真下にローラーがくる位置まで戻す"],
  },
  "ケーブルウッドチョッパー": {
    how: "ケーブルを斜め上(または斜め下)に構え、体を捻りながら対角線上に振り下ろす(または振り上げる)ように動かします。",
    tips: ["腕の力だけでなく体幹の回旋で動かす", "腰ではなく胸から上を捻る意識を持つ", "反動をつけすぎず一定の速度で行う"],
  },
  "ハンギングレッグレイズ": {
    how: "バーにぶら下がった状態から、脚(または膝)をまっすぐ持ち上げ、ゆっくり下ろします。",
    tips: ["反動を使って脚を振らない", "骨盤を後傾させるように上げるとより効く", "肩がすくまないよう肩甲骨を下げておく"],
  },
  "デクラインパイクプッシュアップ": {
    how: "足を台に乗せて腰を高く上げた体勢を作り、頭が床に近づくように肘を曲げて体を沈め、押し上げます。",
    tips: ["肩の真上あたりに手をつく", "頭から着地せず、手で支えられる範囲で行う", "腰を反らさずお腹に力を入れておく"],
  },
  "逆立ち腕立て伏せ": {
    how: "壁を使って逆立ちの姿勢を取り、頭が床に近づくように肘を曲げて体を沈め、押し上げます。",
    tips: ["必ず壁など安全な支えがある状態で行う", "首に負担をかけないよう頭で着地しない", "無理な場合はデクラインパイクプッシュアップから始める"],
  },
  "斜め腕立て伏せ": {
    how: "壁やベンチなど傾斜のある場所に手をつき、体を斜めに保った状態で腕立て伏せを行います。通常より負荷を下げて行える初心者向けの姿勢です。",
    tips: ["傾斜を急にするほど負荷は軽くなる", "体をまっすぐに保ち、腰を反らさない", "慣れてきたら傾斜を緩くして負荷を上げる"],
  },
  "斜め懸垂": {
    how: "バーの下に体を入れ、体を斜めにした状態でバーを胸に引き寄せます。懸垂よりも負荷を下げて行える背中の種目です。",
    tips: ["体をまっすぐに保つ", "バーの高さで負荷を調整できる(低いほど強度が上がる)", "肩甲骨を寄せる意識を持つ"],
  },
  "ディップス": {
    how: "平行棒などに腕を伸ばして体を支え、肘を曲げて体を沈めたあと、腕を伸ばして押し上げます。",
    tips: ["上体を前傾させると胸に、直立に近いと腕により効く", "肩をすくめず肩甲骨を下げておく", "肩に不安がある場合は可動域を浅めにする"],
  },
  "自重スクワット": {
    how: "器具を使わず、肩幅程度に足を開いて立ち、膝とつま先の向きを揃えながら股関節と膝を曲げてしゃがみ、立ち上がります。",
    tips: ["膝がつま先より内側に入らないようにする", "腕を前に伸ばしてバランスを取ってもよい", "回数を多めに行い、フォーム習得や有酸素代わりにも使える"],
  },
  "ダンベルサイドレイズ": {
    how: "腕を体側に下ろした状態から、肘を軽く曲げたまま真横に持ち上げ、肩の高さ付近まで上げます。",
    tips: ["体を反動で振らない", "肩がすくまないよう肩は下げたまま上げる", "小指側からやや上げるイメージで三角筋中部を意識する"],
  },
  "ダンベルフロントレイズ": {
    how: "腕を体の前に下ろした状態から、肘を軽く伸ばしたまま体の前方へ持ち上げ、肩の高さ付近まで上げます。",
    tips: ["体を反らして反動をつけない", "上げすぎず肩の高さ程度で止める", "片方ずつ交互に行うとより効かせやすい"],
  },
  "ダンベルアップライトロー": {
    how: "ダンベルを体の前で持ち、肘を高く上げながら鎖骨の高さまで引き上げます。",
    tips: ["肘を手より高い位置まで引き上げる", "肩に痛みが出る場合は挙げる高さを控えめにする", "ダンベルを体から離しすぎない"],
  },
  "チューブサイドレイズ": {
    how: "チューブを足で踏むか固定し、腕を体側から真横に持ち上げ、肩の高さ付近まで上げます。",
    tips: ["伸ばしきる位置ほど負荷が強くなるため、そこで丁寧に止める", "反動を使わない", "肩をすくめないよう注意する"],
  },
  "チューブフロントレイズ": {
    how: "チューブを足で踏むか固定し、腕を体の前方へ持ち上げ、肩の高さ付近まで上げます。",
    tips: ["伸ばしきる位置ほど負荷が強くなるため、そこで丁寧に止める", "反動を使わない", "上げすぎず肩の高さ程度で止める"],
  },
  "チューブアップライトロー": {
    how: "チューブを足で踏んで両端(または片端)を持ち、肘を高く上げながら鎖骨の高さまで引き上げます。",
    tips: ["肘を手より高い位置まで引き上げる", "肩に痛みが出る場合は挙げる高さを控えめにする", "反動を使わずゆっくり引く"],
  },
  "ケーブルアップライトロー": {
    how: "ケーブルを体の前で持ち、肘を高く上げながら鎖骨の高さまで引き上げます。",
    tips: ["肘を手より高い位置まで引き上げる", "肩に痛みが出る場合は挙げる高さを控えめにする", "ケーブルを体から離しすぎない"],
  },
  "ケーブルフロントレイズ": {
    how: "ケーブルを体の前方(または後方)から取り、腕を伸ばしたまま体の前方へ持ち上げ、肩の高さ付近まで上げます。",
    tips: ["体を反らして反動をつけない", "上げすぎず肩の高さ程度で止める", "ケーブルなので動作の最後まで負荷が抜けにくい"],
  },
  "バーベルロウ": {
    how: "上体を前傾させてバーベルを両手で構え、肘を後ろに引きながらお腹の上あたりに引き寄せます。",
    tips: ["背中を丸めず、まっすぐな姿勢を保つ", "肘から引く意識を持ち、反動を使わない", "戻すときは腕を完全に伸ばしきる"],
  },
  "バーベルベントオーバーロー": {
    how: "上体を深く前傾させてバーベルを両手で構え、肘を後ろに引きながら胸に近い位置まで引き寄せます。",
    tips: ["背中を丸めず、まっすぐな姿勢を保つ", "膝を軽く緩めて股関節を折りたたむ", "反動を使わず一定のテンポで引く"],
  },
};

// 同じ動作パターン(押す・引く・体幹など)に属していても、動きの実態が大きく異なる
// バリエーションについては、こちらで個別に説明文を差し替える。
const MOVEMENT_OVERRIDES = [
  { test: (n) => n.includes("レッグエクステンション"), how: "マシンや器具に座り、膝を支点にすねを前方へ振り上げるようにして脚を伸ばし、ゆっくり戻します。", tips: ["膝の位置とマシンの支点を合わせる", "反動をつけず、伸ばしきったところで一瞬キープする", "足首を反らせすぎない"] },
  { test: (n) => n.includes("レッグカール"), how: "うつ伏せや座った姿勢で、膝を支点にかかとをお尻に近づけるように脚を曲げ、ゆっくり戻します。", tips: ["腰が浮かないよう固定する", "反動を使わずハムストリングスの収縮を意識する", "戻すときも力を抜ききらない"] },
  { test: (n) => n.includes("ハンマーカール"), how: "手のひらを内側に向けたまま(親指が上を向く握り方)、肘を固定して前腕を曲げ、重量を持ち上げます。", tips: ["手首を返さず、ハンマーで叩くような軌道を保つ", "肘を体側から離さない", "腕橈骨筋も一緒に使われる感覚を意識する"] },
  { test: (n) => n.includes("コンセントレーションカール"), how: "座って肘を太ももの内側に固定し、その一点を支点にして前腕を曲げ、重量を持ち上げます。", tips: ["肘の位置を動作中ずっと固定する", "反動を一切使わずゆっくり行う", "収縮位置で一瞬キープすると効果的"] },
  { test: (n) => n.includes("ドラッグカール"), how: "重量を体から離さず、脇腹に沿わせるように真上へ引き上げる(肘を後ろに引くイメージの)カールです。", tips: ["バーやダンベルを体に擦るように上げる", "肘を後方に引く意識を持つ", "巻き上げるような軌道にならないよう注意する"] },
  { test: (n) => n.includes("サイドカール"), how: "腕を体の真横に下ろした状態から、肘を軽く固定して前腕を曲げ、重量を持ち上げます。", tips: ["体を反動で揺らさない", "肘の位置を体側でキープする", "手首を巻き込まずまっすぐ保つ"] },
  { test: (n) => n.includes("フレンチプレス"), how: "重量を頭上に構え、肘の位置を固定したまま頭の後ろへ下ろし、再び伸ばし上げます。", tips: ["肘が左右に開かないよう固定する", "重量を下ろしすぎて肩に負担をかけない", "伸ばしきったところで軽くキープする"] },
  { test: (n) => n.includes("プレスダウン"), how: "肘を体側に固定した状態から、前腕を下方向に押し下げるように伸ばし、ゆっくり戻します。", tips: ["肘を体から離さず固定する", "体を反らして重量を下げない", "伸ばしきった位置で一瞬キープする"] },
  { test: (n) => n.includes("トライセプスエクステンション") || n.includes("トライセプスプレス"), how: "肘を曲げた状態から、その関節をまっすぐ伸ばすように力を加え、ゆっくり戻します。", tips: ["肘の位置が動かないよう固定する", "反動を使わない", "伸ばしきった位置で軽くキープする"] },
  { test: (n) => n.includes("キックバック") && !n.includes("ヒップ"), how: "上体を前傾させ、肘を体側で90度ほどに固定した状態から、前腕を後方へ蹴り出すように伸ばします。", tips: ["肘の高さを動作中ずっとキープする", "反動で振り上げない", "伸ばしきった位置で一瞬止める"] },
  { test: (n) => n.includes("サイドベント"), how: "重量を体の片側に持ち、体を真横に倒してからもとに戻す動作を、体幹の側面を使って行います。", tips: ["前後に体が傾かないよう真横に倒す", "反動を使わず側腹の収縮でコントロールする", "可動域を欲張りすぎない"] },
  { test: (n) => n.includes("ロシアンツイスト") || n.includes("ツイスト"), how: "膝を立てて座り、上体をやや後ろに倒した姿勢を保ちながら、体を左右に捻ります。", tips: ["腰ではなく胸から上を捻る", "足を浮かせると強度が上がる", "反動をつけすぎず一定のテンポで行う"] },
  { test: (n) => n.includes("トゥタッチクランチ"), how: "仰向けで脚を天井方向に伸ばし、手を伸ばしたまま上体を起こしてつま先にタッチするように腹筋を収縮させます。", tips: ["反動を使わず腹筋の力で起き上がる", "首に力を入れすぎない", "戻すときもゆっくりコントロールする"] },
  { test: (n) => n.includes("レッグレイズ") && !n.includes("ハンギング"), how: "仰向けに寝て脚をまっすぐ伸ばし、腹筋を使って脚を天井方向へ持ち上げ、ゆっくり下ろします。", tips: ["腰が反らないよう床に押し付ける意識を持つ", "反動を使わず脚を振らない", "下ろしきる直前で床につけずキープすると強度が上がる"] },
  { test: (n) => n.includes("リバースクランチ"), how: "仰向けで脚を持ち上げ、膝を胸に引き寄せるように骨盤を丸めて腰を浮かせ、ゆっくり戻します。", tips: ["脚の勢いで持ち上げず、腹筋で骨盤を丸める", "首や肩の力みを抜く", "戻すときも反動を使わない"] },
  { test: (n) => n.includes("四の字クランチ"), how: "片足の足首をもう片方の膝に乗せて4の字を作り、その状態で斜め方向へ体を起こすように腹筋を収縮させます。", tips: ["斜め上の肋骨を骨盤に近づけるイメージで起き上がる", "首を手で引っ張らない", "左右均等に行う"] },
  { test: (n) => n.includes("カールアップ"), how: "仰向けで膝を立て、腰を床につけたまま肩甲骨が浮く程度まで上体を丸めて起こし、ゆっくり戻します。", tips: ["腰は床につけたまま行う(通常のクランチより浅い動作)", "首を丸めすぎない", "ゆっくりとしたテンポで行う"] },
  { test: (n) => n.includes("バックエクステンション"), how: "台やマシンにうつ伏せになり、腰から上を反らせるように持ち上げ、ゆっくり戻します。腹筋種目とは逆に、体を反らす動作です。", tips: ["反らしすぎて腰を痛めないよう、まっすぐになる程度で止める", "反動を使わず背筋の収縮を意識する", "首まで反らしすぎない"] },
  { test: (n) => n.includes("リアラテラルレイズ"), how: "上体を前傾させ、腕を軽く曲げたまま左右に開くように持ち上げ、肩の後ろ側を収縮させます。", tips: ["体の反動で持ち上げない", "肘から上げる意識を持つ", "肩がすくまないようにする"] },
  { test: (n) => n.includes("リバースフライ"), how: "上体を前傾させ、腕を左右に開くように持ち上げて、肩甲骨を寄せながら肩の後ろ側を収縮させます。", tips: ["肘を軽く曲げたまま弧を描くように動かす", "反動を使わずゆっくり開く", "肩をすくめず肩甲骨から動かす"] },
  { test: (n) => n.includes("フェイスプル") && n !== "フェイスプル", how: "顔の高さに構えたケーブルやチューブを、肘を左右に大きく開きながら顔に向かって引き寄せます。", tips: ["肘を肩より高い位置まで開く", "引き終わりで肩甲骨をしっかり寄せる", "重すぎる重量より正しい軌道を優先する"] },
  { test: (n) => n.includes("膝つき"), how: "膝を床につけることで負荷を下げた姿勢で腕立て伏せを行います。通常の腕立て伏せより取り組みやすい初心者向けの種目です。", tips: ["体幹はまっすぐを保ち、腰だけを曲げない", "肘を伸ばしきる位置まで押し上げる", "慣れたら膝を浮かせた通常の腕立て伏せに移行する"] },
  { test: (n) => n.includes("足上げ"), how: "足を台に乗せて上半身の傾斜を急にした状態で腕立て伏せを行い、通常より上部への負荷を高めます。", tips: ["台の高さは無理のない範囲で調整する", "腰が反らないようお腹に力を入れる", "肩に痛みが出る場合は台の高さを下げる"] },
  { test: (n) => n.includes("ダイヤモンド"), how: "両手の親指と人差し指でひし形(ダイヤモンド)を作るように近づけて床につき、その状態で腕立て伏せを行います。", tips: ["手幅を狭くする分、手首への負担に注意する", "肘を体側に沿わせるように曲げる", "きつい場合は膝をついて負荷を調整する"] },
  { test: (n) => n.includes("ベンチディップス"), how: "ベンチに両手をつき、脚を前に伸ばした状態でお尻を下ろし、腕を伸ばして押し上げます。", tips: ["肩をすくめず肩甲骨を下げておく", "肘を後方に引くように曲げる", "脚を伸ばすほど負荷が上がるので調整する"] },
  { test: (n) => n.includes("リバースプッシュアップ"), how: "後ろ向きに手をついて体を支え、肘を曲げて腰を落とし、腕を伸ばして押し上げます。", tips: ["肩がすくまないよう肩甲骨を下げる", "肘の角度を90度程度までに抑える", "手首が痛む場合は角度や台の高さを調整する"] },
  { test: (n) => n.includes("マシンクランチ"), how: "マシンに座り、パッドを胸や肩で押し下げるように上体を丸めて腹筋を収縮させ、ゆっくり戻します。", tips: ["腕の力で引き下げず、腹筋の収縮で動かす", "可動域を大きく使う", "戻すときも反動を使わない"] },
  { test: (n) => n.includes("アダクション"), how: "マシンに座り、開いた両脚のパッドを内側に閉じるように寄せ、ゆっくり戻します。", tips: ["反動を使わず内転筋の収縮を意識する", "腰が浮かないよう座面に固定する", "閉じきった位置で一瞬キープする"] },
  { test: (n) => n.includes("テイトプレス"), how: "仰向けでダンベルを胸の上に構え、肘を曲げて重量を胸に近づけたあと、円を描くように腕を伸ばし上げます。", tips: ["肘の軌道を丸く動かすことを意識する", "手首に負担がかかりやすいので重量は控えめに", "反動を使わずゆっくり行う"] },
];

function getExerciseDetail(ex) {
  let how, tips;
  if (EXERCISE_OVERRIDES[ex.name]) {
    how = EXERCISE_OVERRIDES[ex.name].how;
    tips = [...EXERCISE_OVERRIDES[ex.name].tips];
  } else {
    const movementMatch = MOVEMENT_OVERRIDES.find((m) => m.test(ex.name));
    const base = movementMatch ? { how: movementMatch.how, tips: movementMatch.tips } : (PATTERN_INFO[ex.pattern] || { how: "", tips: [] });
    how = base.how;
    tips = [...(base.tips || [])];
    const mods = NAME_MODIFIERS.filter((m) => m.test(ex.name));
    const modLimit = movementMatch ? 1 : 2;
    mods.slice(0, modLimit).forEach((m) => {
      if (m.howAdd) how = how + " " + m.howAdd;
      if (m.tipAdd && !tips.includes(m.tipAdd)) tips = [m.tipAdd, ...tips].slice(0, 4);
    });
  }
  const benchTip = getBenchAngleTip(ex.name);
  if (benchTip && !tips.some((t) => t.includes("ベンチの角度"))) {
    tips = [benchTip, ...tips].slice(0, 5);
  }
  return { how, tips };
}


const PATTERN_LABELS = {
  push_horizontal: "プレス系(押す・水平)",
  push_vertical: "プレス系(押す・上方)",
  pull_vertical: "懸垂系(引く・上方)",
  pull_horizontal: "ロウ系(引く・水平)",
  squat: "スクワット系",
  hinge: "ヒンジ系(股関節)",
  lunge: "ランジ系",
  curl: "カール系(曲げる)",
  extension: "エクステンション系(伸ばす)",
  fly: "フライ系",
  raise: "レイズ系(上げる)",
  core: "体幹・腹筋系",
  shrug: "シュラッグ系",
  calf: "カーフ系",
};

const MovementIcon = React.memo(function MovementIcon({ pattern, size = 26, color = COLORS.steel }) {
  const s = { stroke: color, strokeWidth: 2.4, strokeLinecap: "round", strokeLinejoin: "round", fill: "none" };
  let body = null;
  switch (pattern) {
    case "push_horizontal":
      body = (<>
        <circle cx="8" cy="23" r="3" {...s} />
        <line x1="11" y1="23" x2="24" y2="23" {...s} />
        <path d="M24,23 L29,28 L34,23" {...s} />
        <line x1="16" y1="23" x2="16" y2="13" {...s} />
        <line x1="9" y1="13" x2="23" y2="13" {...s} />
        <circle cx="9" cy="13" r="1.8" {...s} />
        <circle cx="23" cy="13" r="1.8" {...s} />
      </>);
      break;
    case "push_vertical":
      body = (<>
        <circle cx="20" cy="8" r="3" {...s} />
        <line x1="20" y1="11" x2="20" y2="24" {...s} />
        <line x1="20" y1="24" x2="15" y2="35" {...s} />
        <line x1="20" y1="24" x2="25" y2="35" {...s} />
        <line x1="20" y1="14" x2="12" y2="5" {...s} />
        <line x1="20" y1="14" x2="28" y2="5" {...s} />
        <line x1="10" y1="5" x2="30" y2="5" {...s} />
      </>);
      break;
    case "pull_vertical":
      body = (<>
        <line x1="8" y1="4" x2="32" y2="4" {...s} />
        <circle cx="20" cy="10" r="3" {...s} />
        <line x1="20" y1="13" x2="10" y2="4" {...s} />
        <line x1="20" y1="13" x2="30" y2="4" {...s} />
        <line x1="20" y1="13" x2="20" y2="27" {...s} />
        <line x1="20" y1="27" x2="17" y2="36" {...s} />
        <line x1="20" y1="27" x2="23" y2="36" {...s} />
      </>);
      break;
    case "pull_horizontal":
      body = (<>
        <circle cx="10" cy="12" r="3" {...s} />
        <line x1="10" y1="15" x2="26" y2="25" {...s} />
        <line x1="26" y1="25" x2="24" y2="36" {...s} />
        <line x1="26" y1="25" x2="33" y2="29" {...s} />
        <line x1="17" y1="19" x2="11" y2="24" {...s} />
        <circle cx="11" cy="24" r="1.8" {...s} />
      </>);
      break;
    case "squat":
      body = (<>
        <circle cx="20" cy="8" r="3" {...s} />
        <line x1="12" y1="11" x2="28" y2="11" {...s} />
        <line x1="20" y1="11" x2="20" y2="20" {...s} />
        <line x1="20" y1="20" x2="12" y2="26" {...s} />
        <line x1="20" y1="20" x2="28" y2="26" {...s} />
        <line x1="12" y1="26" x2="14" y2="36" {...s} />
        <line x1="28" y1="26" x2="26" y2="36" {...s} />
      </>);
      break;
    case "hinge":
      body = (<>
        <circle cx="9" cy="14" r="3" {...s} />
        <line x1="9" y1="17" x2="28" y2="24" {...s} />
        <line x1="28" y1="24" x2="26" y2="35" {...s} />
        <line x1="28" y1="24" x2="32" y2="34" {...s} />
        <line x1="15" y1="19" x2="15" y2="31" {...s} />
        <line x1="8" y1="31" x2="22" y2="31" {...s} />
        <circle cx="8" cy="31" r="1.8" {...s} />
        <circle cx="22" cy="31" r="1.8" {...s} />
      </>);
      break;
    case "lunge":
      body = (<>
        <circle cx="16" cy="8" r="3" {...s} />
        <line x1="16" y1="11" x2="19" y2="22" {...s} />
        <path d="M19,22 L25,25 L25,35" {...s} />
        <path d="M19,22 L10,27 L8,35" {...s} />
      </>);
      break;
    case "curl":
      body = (<>
        <circle cx="20" cy="8" r="3" {...s} />
        <line x1="20" y1="11" x2="20" y2="26" {...s} />
        <line x1="20" y1="26" x2="16" y2="36" {...s} />
        <line x1="20" y1="26" x2="24" y2="36" {...s} />
        <line x1="20" y1="15" x2="26" y2="19" {...s} />
        <line x1="26" y1="19" x2="22" y2="10" {...s} />
        <circle cx="22" cy="10" r="2" {...s} />
      </>);
      break;
    case "extension":
      body = (<>
        <circle cx="13" cy="8" r="3" {...s} />
        <line x1="13" y1="11" x2="13" y2="26" {...s} />
        <line x1="13" y1="26" x2="9" y2="36" {...s} />
        <line x1="13" y1="26" x2="17" y2="36" {...s} />
        <line x1="13" y1="14" x2="21" y2="11" {...s} />
        <line x1="21" y1="11" x2="21" y2="22" {...s} />
        <circle cx="21" cy="22" r="1.8" {...s} />
      </>);
      break;
    case "fly":
      body = (<>
        <circle cx="20" cy="10" r="3" {...s} />
        <line x1="20" y1="13" x2="20" y2="26" {...s} />
        <line x1="20" y1="26" x2="16" y2="36" {...s} />
        <line x1="20" y1="26" x2="24" y2="36" {...s} />
        <path d="M9,17 Q20,25 31,17" {...s} />
      </>);
      break;
    case "raise":
      body = (<>
        <circle cx="20" cy="8" r="3" {...s} />
        <line x1="20" y1="11" x2="20" y2="26" {...s} />
        <line x1="20" y1="26" x2="16" y2="36" {...s} />
        <line x1="20" y1="26" x2="24" y2="36" {...s} />
        <line x1="20" y1="15" x2="8" y2="13" {...s} />
        <line x1="20" y1="15" x2="32" y2="13" {...s} />
      </>);
      break;
    case "core":
      body = (<>
        <path d="M9,31 L20,27 L27,33" {...s} />
        <path d="M9,31 Q8,20 15,13" {...s} />
        <circle cx="15" cy="10" r="3" {...s} />
      </>);
      break;
    case "shrug":
      body = (<>
        <circle cx="20" cy="9" r="3" {...s} />
        <line x1="20" y1="12" x2="20" y2="26" {...s} />
        <line x1="13" y1="13" x2="27" y2="13" {...s} />
        <line x1="13" y1="13" x2="11" y2="25" {...s} />
        <line x1="27" y1="13" x2="29" y2="25" {...s} />
        <circle cx="11" cy="25" r="1.8" {...s} />
        <circle cx="29" cy="25" r="1.8" {...s} />
        <line x1="20" y1="26" x2="16" y2="36" {...s} />
        <line x1="20" y1="26" x2="24" y2="36" {...s} />
      </>);
      break;
    case "calf":
      body = (<>
        <circle cx="20" cy="8" r="3" {...s} />
        <line x1="20" y1="11" x2="20" y2="24" {...s} />
        <line x1="20" y1="14" x2="13" y2="19" {...s} />
        <line x1="20" y1="14" x2="27" y2="19" {...s} />
        <line x1="20" y1="24" x2="18" y2="32" {...s} />
        <line x1="18" y1="32" x2="23" y2="34" {...s} />
      </>);
      break;
    default:
      body = <circle cx="20" cy="20" r="10" {...s} />;
  }
  return (
    <svg viewBox="0 0 40 40" width={size} height={size} style={{ flexShrink: 0 }}>
      {body}
    </svg>
  );
});

const ExerciseRow = React.memo(function ExerciseRow({ ex, onDetail, showEquipment }) {
  return (
    <div
      style={{
        display: "flex", justifyContent: "space-between", alignItems: "center", background: COLORS.surface,
        border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: "8px 12px", marginBottom: 6,
        contentVisibility: "auto", containIntrinsicSize: "0 46px",
      }}
    >
      <button type="button" onClick={() => onDetail(ex)} style={{ flex: 1, display: "flex", alignItems: "center", gap: 10, background: "none", border: "none", textAlign: "left", padding: 0, color: COLORS.chalk}}>
        <div style={{ background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 6, padding: 3, display: "flex", flexShrink: 0 }}>
          <MovementIcon pattern={ex.pattern} size={20} />
        </div>
        {showEquipment ? (
          <div>
            <div style={{ fontSize: 14 }}>{ex.name}</div>
            <div style={{ fontSize: 10, color: COLORS.steelDim }}>{ex.category} · {EQUIPMENT_LABELS[ex.equipment]}</div>
          </div>
        ) : (
          <span style={{ fontSize: 14 }}>{ex.name}</span>
        )}
      </button>
    </div>
  );
});

export default function Overload() {
  const [loaded, setLoaded] = useState(false);
  const [exercises, setExercises] = useState(DEFAULT_EXERCISES);
  const [sessions, setSessions] = useState([]);
  const [activeSession, setActiveSession] = useState(null);
  const [tab, setTab] = useState("workout");
  const [pickerOpen, setPickerOpen] = useState(false);
  const [pickerCategory, setPickerCategory] = useState(null);
  const [pickerEquipment, setPickerEquipment] = useState(null);
  const [detailExercise, setDetailExercise] = useState(null);
  const [expandedHistory, setExpandedHistory] = useState({});

  const [restSeconds, setRestSeconds] = useState(90);
  const [restRemaining, setRestRemaining] = useState(null);
  const [restRunning, setRestRunning] = useState(false);
  const [restJustFinished, setRestJustFinished] = useState(false);
  const [restNextSet, setRestNextSet] = useState(null);
  const [bodyWeightKg, setBodyWeightKg] = useState(65);
  const [gender, setGender] = useState("none");
  const [userName, setUserName] = useState("");
  const [age, setAge] = useState("");
  const [completionSummary, setCompletionSummary] = useState(null);
  const [confirmAction, setConfirmAction] = useState(null);
  const [weightError, setWeightError] = useState("");
  const [repsError, setRepsError] = useState("");
  const [exerciseSearch, setExerciseSearch] = useState("");
  const [filterCategory, setFilterCategory] = useState("all");
  const [filterEquipment, setFilterEquipment] = useState("all");
  const [historyMonth, setHistoryMonth] = useState(() => {
    const now = new Date();
    return { year: now.getFullYear(), month: now.getMonth() };
  });
  const [selectedDay, setSelectedDay] = useState(null);

  // APIから種目一覧を取得
  useEffect(() => {
    fetch("http://localhost:8080/api/exercises")
      .then((res) => {
        if (!res.ok) {
          throw new Error("種目一覧の取得に失敗しました");
        }
        return res.json();
      })
      .then((data) => {
        console.log("APIから取得した種目:", data);
        setExercises(data);
      })
      .catch((error) => {
        console.error("種目一覧取得エラー:", error);
      });
  }, []);

  // load
  useEffect(() => {
    (async () => {
      try {
        // プロフィールはJava APIから取得
        const userRes = await fetch("http://localhost:8080/api/users/1");

        if (!userRes.ok) {
          throw new Error("プロフィールの取得に失敗しました");
        }

        const user = await userRes.json();

        setUserName(user.name || "");
        setGender(user.gender || "");
        setAge(user.age ?? "");
        setBodyWeightKg(user.bodyWeightKg ?? "");

        console.log("APIから取得したプロフィール:", user);

      } catch (e) {
        console.error("プロフィール・データ読み込みエラー:", e);
      } finally {
        setLoaded(true);
      }
    })();
  }, []);

  // APIから履歴を取得
  useEffect(() => {
    fetch("http://localhost:8080/api/workouts/history?user_id=1")
      .then((res) => {
        if (!res.ok) {
          throw new Error("履歴の取得に失敗しました");
        }
        return res.json();
      })
      .then((data) => {
        console.log("APIから取得した履歴:", data);

		const grouped = {};

		data.forEach((item) => {
		  if (!grouped[item.sessionId]) {
		    grouped[item.sessionId] = {
		      id: item.sessionId,
		      date: item.startedAt,
		      totalCalories: 0,
		      entries: [],
		    };
		  }

		  grouped[item.sessionId].entries.push({
		    exerciseId: item.exerciseId,
		    note: item.note || "",
		    sets: item.sets.map((set) => ({
		      weight: Number(set.weightKg),
		      reps: set.reps,
		    })),
		  });
		});

		const historySessions = Object.values(grouped).map((session) => {
		  const activeMinutes = session.entries.reduce(
		    (sum, entry) =>
		      sum +
		      entry.sets.reduce(
		        (setSum, set) =>
		          setSum + (set.reps * SECONDS_PER_REP) / 60,
		        0
		      ),
		    0
		  );

		  const totalCalories = caloriesForMinutes(
		    WEIGHT_TRAINING_MET,
		    bodyWeightKg,
		    activeMinutes
		  );

		  return {
		    ...session,
		    totalCalories,
		  };
		});
		
		setSessions(
		  historySessions.sort(
		    (a, b) => new Date(b.date) - new Date(a.date)
		  )
		);
      })
      .catch((error) => {
        console.error("履歴取得エラー:", error);
      });
  }, []);

  // プロフィールをJava APIへ自動保存
  useEffect(() => {
    if (!loaded) return;

    const timer = setTimeout(async () => {
      try {
        const res = await fetch("http://localhost:8080/api/users/1", {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            name: userName,
            gender: gender,
            age: age === "" ? null : Number(age),
            bodyWeightKg:
              bodyWeightKg === "" ? null : Number(bodyWeightKg),
          }),
        });

        if (!res.ok) {
          throw new Error("プロフィール保存に失敗しました");
        }

        console.log("プロフィール保存成功");
      } catch (error) {
        console.error("プロフィール保存エラー:", error);
      }
    }, 500);

    return () => clearTimeout(timer);
  }, [userName, gender, age, bodyWeightKg, loaded]);
  
  // rest timer tick
  useEffect(() => {
    if (!restRunning) return;
    if (restRemaining <= 0) {
      setRestRunning(false);
      beep();
      vibrate();
      setRestJustFinished(true);
      setTimeout(() => setRestJustFinished(false), 2500);
      return;
    }
    const t = setTimeout(() => setRestRemaining((r) => r - 1), 1000);
    return () => clearTimeout(t);
  }, [restRunning, restRemaining]);

  const exerciseMap = useMemo(() => {
    const m = {};
    exercises.forEach((e) => (m[e.id] = e));
    return m;
  }, [exercises]);

  const prMap = useMemo(() => {
    const all = [...sessions, ...(activeSession ? [activeSession] : [])];
    const m = {};
    all.forEach((s) => {
      (s.entries || []).forEach((entry) => {
        entry.sets.forEach((set) => {
          if (!m[entry.exerciseId] || set.weight > m[entry.exerciseId]) {
            m[entry.exerciseId] = set.weight;
          }
        });
      });
    });
    return m;
  }, [sessions, activeSession]);

  const exerciseListView = useMemo(() => {
    const q = exerciseSearch.trim().toLowerCase();
    const isFiltering = q || filterCategory !== "all" || filterEquipment !== "all";
    if (!isFiltering) {
      return CATEGORIES.map((cat) => {
        const list = exercises.filter((e) => e.category === cat);
        if (list.length === 0) return null;
        return (
          <div key={cat} style={{ marginBottom: 14 }}>
            <div style={{ fontSize: 11, color: COLORS.chalk, marginBottom: 6, letterSpacing: 1 }}>{cat.toUpperCase()}</div>
            {list.map((ex) => (
              <ExerciseRow key={ex.id} ex={ex} onDetail={setDetailExercise} showEquipment={false} />
            ))}
          </div>
        );
      });
    }
    const results = exercises.filter((e) =>
      (!q || e.name.toLowerCase().includes(q)) &&
      (filterCategory === "all" || e.category === filterCategory) &&
      (filterEquipment === "all" || e.equipment === filterEquipment)
    );
    if (results.length === 0) {
      return <div style={{ textAlign: "center", color: COLORS.steelDim, fontSize: 13, padding: "30px 0" }}>条件に一致する種目が見つかりません</div>;
    }
    return (
      <div style={{ marginBottom: 14 }}>
        <div style={{ fontSize: 11, color: COLORS.chalk, marginBottom: 6, letterSpacing: 1 }}>{results.length}件ヒット</div>
        {results.map((ex) => (
          <ExerciseRow key={ex.id} ex={ex} onDetail={setDetailExercise} showEquipment={true} />
        ))}
      </div>
    );
  }, [exercises, exerciseSearch, filterCategory, filterEquipment]);

  const detailExerciseInfo = useMemo(() => {
    return detailExercise ? getExerciseDetail(detailExercise) : null;
  }, [detailExercise]);

  function startWorkout() {
    setActiveSession({ id: "s-" + Date.now(), date: new Date().toISOString(), entries: [] });
  }

  function addExerciseToSession(exId) {
    setActiveSession((prev) => {
      if (!prev) return prev;
      if (prev.entries.some((e) => e.exerciseId === exId)) return prev;
      return { ...prev, entries: [...prev.entries, { exerciseId: exId, sets: [], draftWeight: "", draftReps: "" }] };
    });
    setPickerOpen(false);
    setPickerCategory(null);
    setPickerEquipment(null);
  }

  function updateDraft(entryIdx, field, value) {
    setActiveSession((prev) => {
      const entries = [...prev.entries];
      entries[entryIdx] = { ...entries[entryIdx], [field]: value };
      return { ...prev, entries };
    });
  }

  function addSet(entryIdx) {
    const entry = activeSession.entries[entryIdx];
	const w = parseFloat(entry.draftWeight);
	const r = parseInt(entry.draftReps, 10);
	const weightInvalid = isNaN(w) || w <= 0;
	const repsInvalid = isNaN(r) || r <= 0;

	setWeightError(
	  weightInvalid ? "重量は0より大きい値を入力してください" : ""
	);

	setRepsError(
	  repsInvalid ? "回数は0より大きい値を入力してください" : ""
	);

	if (weightInvalid || repsInvalid) return;
    const ex = exerciseMap[entry.exerciseId];
    const nextSetNumber = entry.sets.length + 2; // the set just logged is (length+1); the upcoming one is +2
    setActiveSession((prev) => {
      const entries = [...prev.entries];
      const e2 = { ...entries[entryIdx] };
      e2.sets = [...e2.sets, { weight: w, reps: r, id: Date.now() + Math.random() }];
      entries[entryIdx] = e2;
      return { ...prev, entries };
    });
    setRestNextSet({ exerciseName: ex ? ex.name : "", setNumber: nextSetNumber });
    setRestRemaining(restSeconds);
    setRestRunning(true);
  }

  function removeSet(entryIdx, setId) {
    setActiveSession((prev) => {
      const entries = [...prev.entries];
      const entry = { ...entries[entryIdx] };
      entry.sets = entry.sets.filter((s) => s.id !== setId);
      entries[entryIdx] = entry;
      return { ...prev, entries };
    });
  }

  function removeEntry(entryIdx) {
    setActiveSession((prev) => {
      const entries = prev.entries.filter((_, i) => i !== entryIdx);
      return { ...prev, entries };
    });
  }

  async function finishWorkout() {
    if (!activeSession) return;

    const hasSets = activeSession.entries.some((e) => e.sets.length > 0);

    if (hasSets) {
      const activeMinutes = activeSession.entries.reduce(
        (sum, e) =>
          sum +
          e.sets.reduce(
            (s2, set) => s2 + (set.reps * SECONDS_PER_REP) / 60,
            0
          ),
        0
      );

      const elapsedMinutes = Math.max(
        0,
        (Date.now() - new Date(activeSession.date).getTime()) / 60000
      );

      const restMinutes = Math.max(0, elapsedMinutes - activeMinutes);
      const activeCalories = caloriesForMinutes(
        WEIGHT_TRAINING_MET,
        bodyWeightKg,
        activeMinutes
      );
      const restCalories = caloriesForMinutes(
        REST_MET,
        bodyWeightKg,
        restMinutes
      );
      const totalCalories = activeCalories + restCalories;

	  console.log("activeMinutes:", activeMinutes);
	  console.log("bodyWeightKg:", bodyWeightKg);
	  console.log("activeCalories:", activeCalories);
	  console.log("restCalories:", restCalories);
	  console.log("totalCalories:", totalCalories);
	  
      const totalVolume = activeSession.entries.reduce(
        (sum, e) =>
          sum +
          e.sets.reduce(
            (s2, set) => s2 + set.weight * set.reps,
            0
          ),
        0
      );

      const clean = {
        ...activeSession,
        totalCalories,
        entries: activeSession.entries
          .filter((e) => e.sets.length > 0)
          .map(({ draftWeight, draftReps, ...rest }) => rest),
      };

      // ワークアウト保存APIへ送信
      try {
		// noteは種目単位で扱うため、セットごとには設定しない。
		const requestBody = {
		  userId: 1,
		  exercises: clean.entries.map((entry) => ({
		    exerciseId: entry.exerciseId,
		    note: entry.note || "",
		    sets: entry.sets.map((set, index) => ({
		      setNumber: index + 1,
		      weightKg: set.weight,
		      reps: set.reps,
		    })),
		  })),
		};

		const res = await fetch("http://localhost:8080/api/workouts", {
		  method: "POST",
		  headers: {
		    "Content-Type": "application/json",
		  },
		  body: JSON.stringify(requestBody),
		});

		if (!res.ok) {
		  throw new Error("ワークアウト保存に失敗しました");
		}

        console.log("ワークアウト保存成功");
      } catch (error) {
        console.error("ワークアウト保存エラー:", error);
        return;
      }

      setSessions((prev) => [clean, ...prev]);
      setCompletionSummary({
        calories: totalCalories,
        volume: totalVolume,
        exerciseCount: clean.entries.length,
      });
    }

    setActiveSession(null);
    setRestRunning(false);
    setRestRemaining(null);
  }

  if (!loaded) {
    return (
      <div style={{ background: COLORS.bg, minHeight: 500, display: "flex", alignItems: "center", justifyContent: "center", color: COLORS.steel, fontFamily: "Inter, sans-serif" }}>
        読み込み中...
      </div>
    );
  }


  return (
    <div className="overload-root" style={{ background: COLORS.bg, fontFamily: "Inter, sans-serif", color: COLORS.chalk, display: "flex", flexDirection: "column", width: "100%", maxWidth: 480, margin: "0 auto", position: "relative" }}>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Oswald:wght@500;700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap');
        * { box-sizing: border-box; }
        input, select { font-family: inherit; }
        input::placeholder { color: ${COLORS.steelDim}; }
        input[type=number]::-webkit-inner-spin-button, input[type=number]::-webkit-outer-spin-button { -webkit-appearance: none; margin: 0; }
        input[type=number] { -moz-appearance: textfield; appearance: textfield; }
        button { font-family: inherit; cursor: pointer; }
        .btn-tactile { transition: transform 0.08s ease, filter 0.08s ease; }
        .btn-tactile:active { transform: translateY(1.5px); filter: brightness(0.9); }
        .overload-root { min-height: 100vh; min-height: 100dvh; overscroll-behavior: contain; }
        html, body { overscroll-behavior: contain; }
        * { -webkit-tap-highlight-color: transparent; }
        ::-webkit-scrollbar { width: 6px; height: 6px; }
        ::-webkit-scrollbar-thumb { background: ${COLORS.line}; border-radius: 3px; }
        @keyframes pulseGlow { 0%,100% { box-shadow: 0 0 0 0 rgba(214,64,43,0.4);} 50% { box-shadow: 0 0 0 8px rgba(214,64,43,0);} }
        @keyframes restFlash { 0%, 100% { opacity: 0; } 15%, 45% { opacity: 1; } 30%, 60% { opacity: 0; } }
        @keyframes restMsgPop { 0% { opacity: 0; transform: translate(-50%, -50%) scale(0.9); } 10% { opacity: 1; transform: translate(-50%, -50%) scale(1); } 85% { opacity: 1; transform: translate(-50%, -50%) scale(1); } 100% { opacity: 0; transform: translate(-50%, -50%) scale(0.95); } }
      `}</style>

      {/* rest timer complete flash + next-set message */}
      {restJustFinished && (
        <>
          <div style={{ position: "fixed", inset: 0, background: COLORS.red, opacity: 0, pointerEvents: "none", zIndex: 40, animation: "restFlash 1.2s ease-in-out 2" }} />
          <div
            style={{
              position: "fixed", top: "50%", left: "50%", zIndex: 41, pointerEvents: "none",
              animation: "restMsgPop 2.5s ease-in-out forwards",
              background: COLORS.surface, border: `2px solid ${COLORS.red}`, borderRadius: 16,
              padding: "22px 28px", textAlign: "center", maxWidth: "80%", boxShadow: "0 8px 30px rgba(0,0,0,0.5)",
            }}
          >
            <div style={{ fontSize: 26, marginBottom: 6 }}>💪</div>
            {restNextSet && restNextSet.exerciseName && (
              <div style={{ fontSize: 12, color: COLORS.steelDim, marginBottom: 4 }}>{restNextSet.exerciseName}</div>
            )}
            <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 20, color: COLORS.chalk }}>
              {restNextSet ? `${restNextSet.setNumber}セット目を始めましょう!` : "次のセットを始めましょう!"}
            </div>
          </div>
        </>
      )}

	  {/* Header */}
	  <div style={{ padding: "16px 18px 12px", borderBottom: `1px solid ${COLORS.line}`, display: "flex", alignItems: "flex-start" }}>
	    <div style={{ flex: 1, minWidth: 0, textAlign: "left" }}>
	      <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 22, letterSpacing: 1.5, fontWeight: 700, color: COLORS.chalk, textShadow: `0 0 18px rgba(214,64,43,0.45)` }}>OVERLOAD</div>
		  <div style={{ display: "flex", alignItems: "center", gap: 5, fontSize: 11, color: COLORS.steelDim, marginTop: 2, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>
		  {tab === "workout" && <Dumbbell size={11} strokeWidth={1.8} style={{ flexShrink: 0 }} />}
		  {tab === "history" && <History size={11} strokeWidth={1.8} style={{ flexShrink: 0 }} />}
		  {tab === "exercises" && <ListChecks size={11} strokeWidth={1.8} style={{ flexShrink: 0 }} />}
		  {tab === "profile" && <User size={11} strokeWidth={1.8} style={{ flexShrink: 0 }} />}
		  {tab === "history"
		    ? selectedDay !== null
		      ? (() => {
		          const selectedDateSessions = sessions.filter((s) => {
		            const d = new Date(s.date);
		            return (
		              d.getFullYear() === historyMonth.year &&
		              d.getMonth() === historyMonth.month &&
		              d.getDate() === selectedDay
		            );
		          });

		          const exerciseIds = new Set(
		            selectedDateSessions.flatMap((s) =>
		              s.entries.map((e) => e.exerciseId)
		            )
		          );

		          const totalExercises = exerciseIds.size;

		          const totalVolume = selectedDateSessions.reduce(
		            (sum, s) =>
		              sum +
		              s.entries.reduce(
		                (entrySum, e) =>
		                  entrySum +
		                  e.sets.reduce(
		                    (setSum, st) => setSum + st.weight * st.reps,
		                    0
		                  ),
		                0
		              ),
		            0
		          );

		          const totalCalories = selectedDateSessions.reduce(
		            (sum, s) => sum + (s.totalCalories ?? 0),
		            0
		          );

		          return `${fmtDate(
		            new Date(historyMonth.year, historyMonth.month, selectedDay)
		          )} · ${totalExercises}種目 · 総量 ${totalVolume.toLocaleString()}kg · 🔥${totalCalories.toFixed(1)}kcal`;
		        })()
		      : `${historyMonth.year}年${historyMonth.month + 1}月 · 日付を選択してください`
		    : tab === "workout"
		    ? "今日のトレーニングを始めよう"
		    : tab === "exercises"
		    ? "種目を選択すると詳細を確認できます"
		    : tab === "profile"
		    ? "あなたの情報を管理"
		    : ""}
	      </div>
	    </div>
	    {restRemaining !== null && (() => {
	      const gaugeColor = !restRunning ? COLORS.steelDim : restRemaining <= 5 ? COLORS.red : COLORS.amber;
	      const r = 21, circumference = 2 * Math.PI * r;
	      const progress = restSeconds > 0 ? Math.max(0, Math.min(1, restRemaining / restSeconds)) : 0;
	      const dashoffset = circumference * (1 - progress);
	      return (
			<div
			  onClick={() => setRestRunning((r2) => !r2)}
			  style={{ position: "relative", width: 52, height: 52, flexShrink: 0, marginLeft: "auto", display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer", animation: restRunning && restRemaining <= 5 ? "pulseGlow 1s infinite" : "none", borderRadius: "50%" }}
			>
	          <svg width="52" height="52" viewBox="0 0 52 52" style={{ position: "absolute", top: 0, left: 0, transform: "rotate(-90deg)" }}>
	            <circle cx="26" cy="26" r={r} fill="none" stroke={COLORS.line} strokeWidth="4" />
	            <circle
	              cx="26" cy="26" r={r} fill="none" stroke={gaugeColor} strokeWidth="4" strokeLinecap="round"
	              strokeDasharray={circumference} strokeDashoffset={dashoffset}
	              style={{ transition: "stroke-dashoffset 1s linear, stroke 0.3s" }}
	            />
	          </svg>
	          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 1 }}>
	            {restRunning ? <Pause size={9} color={COLORS.steelDim} /> : <Play size={9} color={COLORS.steelDim} />}
	            <span style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 12, fontWeight: 500, letterSpacing: 0.5, color: gaugeColor, textShadow: `0 0 6px ${gaugeColor}88` }}>
	              {Math.floor(restRemaining / 60)}:{String(restRemaining % 60).padStart(2, "0")}
	            </span>
	          </div>
	        </div>
	      );
	    })()}
	  </div>

      {/* Body */}
      <div style={{ flex: 1, overflowY: (pickerOpen || detailExercise || completionSummary) ? "hidden" : "auto", scrollbarGutter: "stable", padding: 16, paddingBottom: 90 }}>
        {tab === "workout" && (
          <>
            {!activeSession ? (
              <div style={{ textAlign: "center", padding: "44px 20px", border: `1.5px dashed ${COLORS.line}`, borderRadius: 16, margin: "8px 0" }}>
                <div style={{ display: "inline-block", fontFamily: "'Oswald', sans-serif", fontSize: 10, letterSpacing: 2, color: COLORS.amber, background: "rgba(240,168,46,0.1)", border: `1px solid ${COLORS.amber}`, borderRadius: 4, padding: "3px 10px", marginBottom: 16 }}>READY</div>
                <div>
                  <Dumbbell size={40} color={COLORS.steelDim} style={{ marginBottom: 14, filter: `drop-shadow(0 0 10px rgba(240,168,46,0.35))` }} />
                </div>
                <div style={{ color: COLORS.steel, fontSize: 14, marginBottom: 20 }}>今日のワークアウトを開始しましょう</div>
                <button type="button" className="btn-tactile" onClick={startWorkout} style={{ background: COLORS.red, color: "#fff", border: "none", borderRadius: 10, padding: "14px 28px", fontSize: 15, fontWeight: 600, boxShadow: `0 4px 20px rgba(214,64,43,0.4)` }}>
                  ワークアウト開始
                </button>
              </div>
            ) : (
              <>
			    {/* rest timer settings */}
			    <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 10, padding: "10px 14px", marginBottom: 10 }}>

			      <div style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 12, color: COLORS.steel }}>

			        <Timer size={14} /> 休憩設定

			      </div>

			      <div style={{ display: "flex", alignItems: "center", gap: 6 }}>

				    <input
				      type="number"
				      min="1"
				      value={restSeconds === 0 ? "" : restSeconds}
				      placeholder="休憩時間を入力"
				      onChange={(e) => setRestSeconds(Number(e.target.value))}
				      style={{ width: 100, height: 30, background: COLORS.surfaceAlt, border: `1px solid ${COLORS.line}`, borderRadius: 6, color: COLORS.chalk, textAlign: "center", fontFamily: "'JetBrains Mono', monospace", fontSize: 13 }}
				    />

			        <span style={{ fontSize: 12, color: COLORS.steel }}>秒</span>

			      </div>

			    </div>

                {activeSession.entries.map((entry, idx) => {
                  const ex = exerciseMap[entry.exerciseId];
                  if (!ex) return null;
                  const pr = prMap[ex.id];
                  return (
                    <div key={entry.exerciseId} style={{ background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 12, padding: 14, marginBottom: 12 }}>
                      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                          <button type="button" onClick={() => setDetailExercise(ex)} style={{ background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: 4, display: "flex" }}>
                            <MovementIcon pattern={ex.pattern} size={26} />
                          </button>
                          <div>
                            <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 17, fontWeight: 500 }}>{ex.name}</div>
                            <div style={{ fontSize: 11, color: COLORS.steelDim }}>{ex.category}{pr ? ` · 自己ベスト ${pr}kg` : ""}</div>
                          </div>
                        </div>
                        <button type="button" onClick={() => removeEntry(idx)} style={{ background: "none", border: "none", color: COLORS.steelDim }}><X size={16} /></button>
                      </div>

                      <div style={{ display: "flex", alignItems: "flex-start", gap: 6, marginTop: 10 }}>
                        <span style={{ fontSize: 13, flexShrink: 0, marginTop: 6 }}>📝</span>
                        <textarea
                          rows={4} placeholder="今日のメモ(履歴に残ります)" maxLength={100}
                          value={entry.note || ""}
                          onChange={(e) => updateDraft(idx, "note", e.target.value)}
                          style={{ flex: 1, background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 6, padding: "6px 8px", color: COLORS.amber, fontSize: 12, fontFamily: "inherit", resize: "none", lineHeight: 1.5 }}
                        />
                      </div>

                      {entry.sets.length > 0 && (
                        <div style={{ marginTop: 10, display: "flex", flexDirection: "column", gap: 6 }}>
                          {entry.sets.map((s, si) => {
                            const isPr = s.weight === pr;
                            return (
                            <div key={s.id} style={{ display: "flex", alignItems: "center", justifyContent: "space-between", background: isPr ? "rgba(240,168,46,0.08)" : COLORS.surfaceAlt, border: isPr ? `1px solid ${COLORS.amber}` : "1px solid transparent", borderRadius: 8, padding: "6px 10px" }}>
                              <span style={{ fontSize: 11, color: COLORS.steelDim, width: 20 }}>#{si + 1}</span>
                              <span style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 13, letterSpacing: 0.8, flex: 1, color: COLORS.amber, textShadow: `0 0 8px rgba(240,168,46,0.45)` }}>{s.weight}kg × {s.reps}回</span>
                              <span style={{ fontSize: 11, color: COLORS.steelDim, marginRight: 8 }}>🔥{estimateSetCalories(s.reps, bodyWeightKg).toFixed(1)}kcal</span>
                              {isPr && (
                                <span style={{
                                  display: "flex", alignItems: "center", gap: 3, background: COLORS.amber, color: COLORS.bg,
                                  fontFamily: "'Oswald', sans-serif", fontSize: 10, fontWeight: 700, letterSpacing: 0.5,
                                  padding: "2px 7px", borderRadius: 5, marginRight: 8, boxShadow: `0 0 10px rgba(240,168,46,0.6)`,
                                }}>
                                  ⚡PR
                                </span>
                              )}
                              <button type="button" onClick={() => removeSet(idx, s.id)} style={{ background: "none", border: "none", color: COLORS.steelDim }}><Trash2 size={13} /></button>
                            </div>
                          );})}
                        </div>
                      )}

					  <div style={{ display: "flex", gap: 8, marginTop: 10, alignItems: "flex-start" }}>
					  <div style={{ flex: 1 }}>
					    <input
					      type="number"
					      inputMode="decimal"
					      placeholder="重量(kg)"
					      value={entry.draftWeight}
					      onChange={(e) => updateDraft(idx, "draftWeight", e.target.value)}
					      style={{
					        width: "100%",
					        boxSizing: "border-box",
					        background: COLORS.bg,
					        border: `1px solid ${COLORS.line}`,
					        borderRadius: 8,
					        padding: "10px",
					        color: COLORS.chalk,
					        fontSize: 14
					      }}
					    />

					    {weightError && (
					      <div style={{ marginTop: 4, fontSize: 11, color: COLORS.red }}>
					        {weightError}
					      </div>
					    )}
					  </div>
					  <div style={{ flex: 1 }}>
					    <input
					      type="number"
					      inputMode="numeric"
					      placeholder="回数"
					      value={entry.draftReps}
					      onChange={(e) => updateDraft(idx, "draftReps", e.target.value)}
					      style={{
					        width: "100%",
					        boxSizing: "border-box",
					        background: COLORS.bg,
					        border: `1px solid ${COLORS.line}`,
					        borderRadius: 8,
					        padding: "10px",
					        color: COLORS.chalk,
					        fontSize: 14
					      }}
					    />

					    {repsError && (
					      <div style={{ marginTop: 4, fontSize: 11, color: COLORS.red }}>
					        {repsError}
					      </div>
					    )}
					  </div>
                        <button type="button" className="btn-tactile" onClick={() => addSet(idx)} style={{ background: COLORS.blue, border: "none", borderRadius: 8, padding: "0 16px", color: "#fff", height: 40 }}>
                          <Plus size={16} />
                        </button>
                      </div>

                      {ex.barbell && (
                        <div style={{ marginTop: 10, fontSize: 11, color: COLORS.steelDim }}>
                          🏋 シャフト: {BAR_WEIGHT}kg
                        </div>
                      )}
                    </div>
                  );
                })}

                <button type="button"
                  onClick={() => { setPickerOpen(true); setPickerCategory(null); setPickerEquipment(null); }}
                  style={{ width: "100%", background: COLORS.surface, border: `1px dashed ${COLORS.line}`, borderRadius: 10, padding: "12px", color: COLORS.steel, fontSize: 13, display: "flex", alignItems: "center", justifyContent: "center", gap: 6, marginBottom: 14 }}
                >
                  <Plus size={14} /> 種目を追加
                </button>

                <button type="button" className="btn-tactile" onClick={finishWorkout} style={{ width: "100%", background: COLORS.green, border: "none", borderRadius: 10, padding: "13px", color: "#fff", fontSize: 14, fontWeight: 600 }}>
                  ワークアウトを終了して保存
                </button>
              </>
            )}
          </>
        )}

        {tab === "history" && (
          <>
            {(() => {
              const now = new Date();
              const isCurrentMonth = historyMonth.year === now.getFullYear() && historyMonth.month === now.getMonth();
              const monthKey = historyMonth.year * 12 + historyMonth.month;
              const earliestKey = sessions.length > 0
                ? Math.min(...sessions.map((s) => { const d = new Date(s.date); return d.getFullYear() * 12 + d.getMonth(); }))
                : monthKey;
              const isEarliestMonth = monthKey <= earliestKey;
              const monthSessions = sessions.filter((s) => {
                const d = new Date(s.date);
                return d.getFullYear() === historyMonth.year && d.getMonth() === historyMonth.month;
              });
              const workoutDays = new Set(monthSessions.map((s) => new Date(s.date).toDateString())).size;
              const monthCalories = monthSessions.reduce((sum, s) => sum + (s.totalCalories ?? 0), 0);
              const goPrev = () => {
                if (isEarliestMonth) return;
                setSelectedDay(null);
                setHistoryMonth((p) => (p.month === 0 ? { year: p.year - 1, month: 11 } : { year: p.year, month: p.month - 1 }));
              };
              const goNext = () => {
                if (isCurrentMonth) return;
                setSelectedDay(null);
                setHistoryMonth((p) => (p.month === 11 ? { year: p.year + 1, month: 0 } : { year: p.year, month: p.month + 1 }));
              };
              return (
                <>
                  <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 14 }}>
                    <button type="button" onClick={goPrev} disabled={isEarliestMonth} style={{ background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 8, width: 34, height: 34, color: isEarliestMonth ? COLORS.steelDim : COLORS.chalk, display: "flex", alignItems: "center", justifyContent: "center", opacity: isEarliestMonth ? 0.4 : 1 }}>
                      <ChevronLeft size={16} />
                    </button>
                    <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 17 }}>{historyMonth.year}年{historyMonth.month + 1}月</div>
                    <button type="button" onClick={goNext} disabled={isCurrentMonth} style={{ background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 8, width: 34, height: 34, color: isCurrentMonth ? COLORS.steelDim : COLORS.chalk, display: "flex", alignItems: "center", justifyContent: "center", opacity: isCurrentMonth ? 0.4 : 1 }}>
                      <ChevronLeft size={16} style={{ transform: "rotate(180deg)" }} />
                    </button>
                  </div>

                  <div style={{ display: "flex", gap: 10, marginBottom: 16 }}>
                    <div style={{ flex: 1, background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 12, padding: 14, textAlign: "center" }}>
                      <div style={{ fontSize: 10, color: COLORS.steelDim, marginBottom: 4 }}>ワークアウト日数</div>
                      <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 22, color: COLORS.red }}>{workoutDays}<span style={{ fontSize: 13, marginLeft: 2 }}>日</span></div>
                    </div>
                    <div style={{ flex: 1, background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 12, padding: 14, textAlign: "center" }}>
                      <div style={{ fontSize: 10, color: COLORS.steelDim, marginBottom: 4 }}>合計消費カロリー</div>
                      <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 22 }}>{monthCalories.toFixed(0)}<span style={{ fontSize: 13, marginLeft: 2 }}>kcal</span></div>
                    </div>
                  </div>

                  {(() => {
                    const firstOfMonth = new Date(historyMonth.year, historyMonth.month, 1);
                    const firstDow = firstOfMonth.getDay();
                    const daysInMonth = new Date(historyMonth.year, historyMonth.month + 1, 0).getDate();
                    const dayLabels = ["日", "月", "火", "水", "木", "金", "土"];
                    const cells = [];
                    for (let i = 0; i < firstDow; i++) cells.push(null);
                    for (let d = 1; d <= daysInMonth; d++) cells.push(d);

                    return (
                      <>
                        <div style={{ display: "grid", gridTemplateColumns: "repeat(7, 1fr)", gap: 4, marginBottom: 6 }}>
						  {dayLabels.map((l, i) => (
						    <div
						      key={l}
						      style={{
						        color: i === 0 ? "#E89A9A" :
						               i === 6 ? "#8CC9F2" :
						               "#B8B8B8",
						        fontSize: 11,
						        textAlign: "center"
						      }}
						    >
						      {l}
						    </div>
						  ))}
                        </div>
                        <div style={{ display: "grid", gridTemplateColumns: "repeat(7, 1fr)", gap: 4, marginBottom: 18 }}>
                          {cells.map((d, i) => {
                            if (d === null) return <div key={"blank" + i} />;
                            const daySessions = monthSessions.filter((s) => new Date(s.date).getDate() === d);
                            const hasSession = daySessions.length > 0;
                            const isPrDay = daySessions.some((s) =>
                              s.entries.some((e) => e.sets.some((set) => set.weight === prMap[e.exerciseId]))
                            );
                            const isSelected = selectedDay === d;
							const date = new Date(historyMonth.year, historyMonth.month, d);
							const dayOfWeek = date.getDay();
							const holidays = [
							  "2026-01-01",
							  "2026-01-12",
							  "2026-02-11",
							  "2026-02-23",
							  "2026-03-20",
							  "2026-04-29",
							  "2026-05-03",
							  "2026-05-04",
							  "2026-05-05",
							  "2026-07-20",
							  "2026-08-11",
							  "2026-09-21",
							  "2026-09-22",
							  "2026-10-12",
							  "2026-11-03",
							  "2026-11-23",
							];

							const dateKey =
							  `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;

							const isHoliday = holidays.includes(dateKey);

							const dayColor =
							  dayOfWeek === 0 || isHoliday ? "#E89A9A" :
							  dayOfWeek === 6 ? "#8CC9F2" :
							  "#B8B8B8";
                            const accentColor = isPrDay ? COLORS.amber : COLORS.red;
                            const selectColor = hasSession ? accentColor : COLORS.steel;
                            return (
                              <button
                                type="button" key={d}
                                onClick={() => setSelectedDay((prev) => (prev === d ? null : d))}
                                style={{
                                  aspectRatio: "1", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center",
                                  borderRadius: 8, fontSize: 12, background: isSelected ? `${selectColor}22` : hasSession ? `${accentColor}1A` : "transparent",
                                  border: `1px solid ${isSelected ? selectColor : hasSession ? accentColor : "transparent"}`,
                                  color: dayColor, cursor: "pointer",
                                }}
                              >
                                <span>{d}</span>
                                {hasSession && <div style={{ width: 4, height: 4, borderRadius: "50%", background: accentColor, marginTop: 2 }} />}
                              </button>
                            );
                          })}
                        </div>
                        <div style={{ display: "flex", gap: 14, marginBottom: 18, paddingBottom: 14, borderBottom: `1px solid ${COLORS.line}` }}>
                          <div style={{ display: "flex", alignItems: "center", gap: 5 }}>
                            <div style={{ width: 8, height: 8, borderRadius: "50%", background: COLORS.red }} />
                            <span style={{ color: COLORS.steelDim, fontSize: 11 }}>記録した日</span>
                          </div>
                          <div style={{ display: "flex", alignItems: "center", gap: 5 }}>
                            <div style={{ width: 8, height: 8, borderRadius: "50%", background: COLORS.amber }} />
                            <span style={{ color: COLORS.steelDim, fontSize: 11 }}>自己ベスト達成</span>
                          </div>
                        </div>

                        {selectedDay === null ? (
                          monthSessions.length === 0 ? (
                            <div style={{ textAlign: "center", padding: "30px 20px", color: COLORS.steelDim, fontSize: 13 }}>
                              <History size={28} style={{ marginBottom: 10 }} />
                              <div>この月の記録はありません</div>
                            </div>
                          ) : (
                            <div style={{ textAlign: "center", padding: "10px 20px", color: COLORS.steelDim, fontSize: 12 }}>
                              日付をタップすると、その日の記録が見られます
                            </div>
                          )
                        ) : (() => {
                          const daySessions = monthSessions.filter((s) => new Date(s.date).getDate() === selectedDay);
                          if (daySessions.length === 0) {
                            return (
                              <div style={{ textAlign: "center", padding: "24px 20px", color: COLORS.steelDim, fontSize: 12 }}>
                                {historyMonth.month + 1}月{selectedDay}日はワークアウトの記録がありません
                              </div>
                            );
                          }
                          return daySessions.map((s) => {
                            const totalVolume = s.entries.reduce((sum, e) => sum + e.sets.reduce((ss, st) => ss + st.weight * st.reps, 0), 0);
                            return (
                              <div key={s.id} style={{ background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 12, marginBottom: 10, padding: 14 }}>
                                <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 15, marginBottom: 2 }}>{fmtDate(s.date)}</div>
                                <div style={{ fontSize: 11, color: COLORS.steelDim, marginBottom: 12 }}>
                                  {s.entries.length}種目 · 総量 {totalVolume.toLocaleString()}kg · 🔥{(s.totalCalories ?? 0).toFixed(0)}kcal
                                </div>
                                {s.entries.map((entry) => {
                                  const ex = exerciseMap[entry.exerciseId];
                                  return (
                                    <div key={entry.exerciseId} style={{ marginBottom: 10 }}>
                                      <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 4 }}>{ex ? ex.name : "削除済み種目"}</div>
                                      <div style={{ display: "flex", flexWrap: "wrap", gap: 6, marginBottom: entry.note ? 6 : 0 }}>
                                        {entry.sets.map((set, i) => (
                                          <span key={i} style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 12, background: COLORS.surfaceAlt, padding: "3px 8px", borderRadius: 6, color: COLORS.steel }}>
                                            {set.weight}kg×{set.reps}
                                          </span>
                                        ))}
                                      </div>
                                      {entry.note && (
                                        <div style={{ fontSize: 12, color: COLORS.amber, display: "flex", alignItems: "flex-start", gap: 5 }}>
                                          <span>📝</span><span style={{ whiteSpace: "pre-wrap", wordBreak: "break-word" }}>{entry.note}</span>
                                        </div>
                                      )}
                                    </div>
                                  );
                                })}
                              </div>
                            );
                          });
                        })()}
                      </>
                    );
                  })()}
                </>
              );
            })()}
          </>
        )}

        {tab === "exercises" && (
          <>
            <div style={{ position: "relative", marginBottom: 10 }}>
              <input
                placeholder="種目名で検索" value={exerciseSearch} onChange={(e) => setExerciseSearch(e.target.value)}
                style={{ width: "100%", background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 10, padding: "12px 14px", color: COLORS.chalk, fontSize: 14 }}
              />
              {exerciseSearch && (
                <button type="button" onClick={() => setExerciseSearch("")} style={{ position: "absolute", right: 10, top: "50%", transform: "translateY(-50%)", background: "none", border: "none", color: COLORS.steelDim }}>
                  <X size={16} />
                </button>
              )}
            </div>

            <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
              <select value={filterCategory} onChange={(e) => setFilterCategory(e.target.value)} style={{ flex: 1, background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 10, padding: "10px 8px", color: COLORS.chalk, fontSize: 13 }}>
                <option value="all">すべての部位</option>
                {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
              </select>
              <select value={filterEquipment} onChange={(e) => setFilterEquipment(e.target.value)} style={{ flex: 1, background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 10, padding: "10px 8px", color: COLORS.chalk, fontSize: 13 }}>
                <option value="all">すべての器具</option>
                {EQUIPMENT_ORDER.map((eq) => <option key={eq} value={eq}>{EQUIPMENT_LABELS[eq]}</option>)}
              </select>
            </div>

            {exerciseListView}
          </>
        )}

        {tab === "profile" && (
          <>
            <div style={{ textAlign: "center", marginBottom: 20 }}>
              <div style={{ width: 64, height: 64, borderRadius: "50%", background: COLORS.surfaceAlt, border: `1px solid ${COLORS.line}`, display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 10px" }}>
                <User size={28} color={COLORS.steel} />
              </div>
              <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 18 }}>{userName.trim() ? userName : "マイプロフィール"}</div>
              <div style={{ fontSize: 11, color: COLORS.steelDim, marginTop: 2 }}>消費カロリー計算にはここで入力した体重が使われます</div>
            </div>

            <div style={{ background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 12, padding: 16, marginBottom: 14 }}>
              <div style={{ marginBottom: 14 }}>
                <div style={{ fontSize: 12, color: COLORS.steel, marginBottom: 6 }}>名前</div>
                <input
                  type="text" placeholder="未設定" value={userName}
                  onChange={(e) => setUserName(e.target.value)}
                  style={{ width: "100%", background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: 10, color: COLORS.chalk, fontSize: 14 }}
                />
              </div>

              <div style={{ marginBottom: 14 }}>
                <div style={{ fontSize: 12, color: COLORS.steel, marginBottom: 6 }}>性別</div>
                <div style={{ display: "flex", gap: 8 }}>
                  {[{ v: "male", l: "男性" }, { v: "female", l: "女性" }, { v: "none", l: "未設定" }].map((g) => (
                    <button type="button" key={g.v} onClick={() => setGender(g.v)}
                      style={{ flex: 1, background: gender === g.v ? COLORS.blue : COLORS.surfaceAlt, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: "10px 0", color: gender === g.v ? "#fff" : COLORS.steel, fontSize: 13 }}
                    >
                      {g.l}
                    </button>
                  ))}
                </div>
              </div>

              <div style={{ marginBottom: 14 }}>
                <div style={{ fontSize: 12, color: COLORS.steel, marginBottom: 6 }}>年齢</div>
                <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <input
                    type="number" inputMode="numeric" placeholder="未設定" value={age}
                    onChange={(e) => setAge(e.target.value === "" ? "" : Math.max(1, parseInt(e.target.value, 10) || 0))}
                    style={{ flex: 1, background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: 10, color: COLORS.chalk, fontSize: 14 }}
                  />
                  <span style={{ fontSize: 12, color: COLORS.steelDim }}>歳</span>
                </div>
              </div>

              <div>
                <div style={{ fontSize: 12, color: COLORS.steel, marginBottom: 6 }}>体重(消費カロリー計算用)</div>
                <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <input
                    type="number" inputMode="decimal" value={bodyWeightKg}
                    onChange={(e) => setBodyWeightKg(e.target.value === "" ? "" : Math.max(0, parseFloat(e.target.value) || 0))}
                    onBlur={() => { if (bodyWeightKg === "" || bodyWeightKg === 0) setBodyWeightKg(65); }}
                    style={{ flex: 1, background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: 10, color: COLORS.chalk, fontSize: 14 }}
                  />
                  <span style={{ fontSize: 12, color: COLORS.steelDim }}>kg</span>
                </div>
              </div>
            </div>

            <div style={{ fontSize: 11, color: COLORS.steelDim, lineHeight: 1.6 }}>
              ※性別・年齢は現在プロフィール表示用として保存されます。消費カロリーの計算式(METs法)には体重のみが使われています。
            </div>
          </>
        )}
      </div>

      {/* exercise picker modal */}
      {pickerOpen && (
        <div
          style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.6)", display: "flex", alignItems: "flex-end", zIndex: 10 }}
          onClick={() => { setPickerOpen(false); setPickerCategory(null); setPickerEquipment(null); }}
        >
          <div onClick={(e) => e.stopPropagation()} style={{ background: COLORS.surface, borderRadius: "16px 16px 0 0", padding: 16, width: "100%", maxHeight: "75%", overflowY: "auto" }}>
            {!pickerCategory ? (
              <>
                <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 16, marginBottom: 12 }}>部位を選択</div>
                {CATEGORIES.map((cat) => {
                  const count = exercises.filter((e) => e.category === cat).length;
                  if (count === 0) return null;
                  return (
                    <button type="button"
                      key={cat} onClick={() => setPickerCategory(cat)}
                      style={{ width: "100%", textAlign: "left", background: COLORS.surfaceAlt, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: "14px 14px", marginBottom: 6, color: COLORS.chalk, fontSize: 15, fontWeight: 600, display: "flex", justifyContent: "space-between", alignItems: "center" }}
                    >
                      <span>{cat}</span>
                      <span style={{ fontSize: 11, color: COLORS.steelDim, fontWeight: 400 }}>{count}種目</span>
                    </button>
                  );
                })}
              </>
            ) : !pickerEquipment ? (
              <>
                <button type="button"
                  onClick={() => setPickerCategory(null)}
                  style={{ background: "none", border: "none", color: COLORS.steel, fontSize: 13, display: "flex", alignItems: "center", gap: 4, padding: 0, marginBottom: 10 }}
                >
                  <ChevronLeft size={15} /> 部位一覧に戻る
                </button>
                <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 16, marginBottom: 12 }}>{pickerCategory} · 器具を選択</div>
                {EQUIPMENT_ORDER.map((eq) => {
                  const count = exercises.filter((e) => e.category === pickerCategory && e.equipment === eq).length;
                  if (count === 0) return null;
                  return (
                    <button type="button"
                      key={eq} onClick={() => setPickerEquipment(eq)}
                      style={{ width: "100%", textAlign: "left", background: COLORS.surfaceAlt, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: "14px 14px", marginBottom: 6, color: COLORS.chalk, fontSize: 15, fontWeight: 600, display: "flex", justifyContent: "space-between", alignItems: "center" }}
                    >
                      <span>{EQUIPMENT_LABELS[eq]}</span>
                      <span style={{ fontSize: 11, color: COLORS.steelDim, fontWeight: 400 }}>{count}種目</span>
                    </button>
                  );
                })}
              </>
            ) : (
              <>
                <button type="button"
                  onClick={() => setPickerEquipment(null)}
                  style={{ background: "none", border: "none", color: COLORS.steel, fontSize: 13, display: "flex", alignItems: "center", gap: 4, padding: 0, marginBottom: 10 }}
                >
                  <ChevronLeft size={15} /> 器具一覧に戻る
                </button>
                <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 16, marginBottom: 12 }}>{pickerCategory} · {EQUIPMENT_LABELS[pickerEquipment]}</div>
                {exercises.filter((ex) => ex.category === pickerCategory && ex.equipment === pickerEquipment).map((ex) => (
                  <div
                    key={ex.id}
                    style={{ display: "flex", alignItems: "center", gap: 8, background: COLORS.surfaceAlt, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: "6px 6px 6px 6px", marginBottom: 6 }}
                  >
                    <button
                      type="button"
                      onClick={() => setDetailExercise(ex)}
                      style={{ background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 6, padding: 3, display: "flex", flexShrink: 0 }}
                    >
                      <MovementIcon pattern={ex.pattern} size={22} />
                    </button>
                    <button
                      type="button"
                      onClick={() => addExerciseToSession(ex.id)}
                      style={{ flex: 1, textAlign: "left", background: "none", border: "none", padding: "4px 4px", color: COLORS.chalk, fontSize: 14, display: "flex", alignItems: "center", justifyContent: "space-between" }}
                    >
                      <span>{ex.name}</span>
                    </button>
                  </div>
                ))}
              </>
            )}
          </div>
        </div>
      )}

      {/* exercise detail modal */}
      {detailExercise && (
        <div
          style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.65)", display: "flex", alignItems: "flex-end", zIndex: 20 }}
          onClick={() => setDetailExercise(null)}
        >
          <div onClick={(e) => e.stopPropagation()} style={{ background: COLORS.surface, borderRadius: "16px 16px 0 0", padding: 18, width: "100%", maxHeight: "80%", overflowY: "auto" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 14 }}>
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <div style={{ background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 10, padding: 8, display: "flex" }}>
                  <MovementIcon pattern={detailExercise.pattern} size={40} />
                </div>
                <div>
                  <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 19, fontWeight: 600 }}>{detailExercise.name}</div>
                  <div style={{ fontSize: 11, color: COLORS.steelDim, marginTop: 2 }}>
                    {detailExercise.category} · {EQUIPMENT_LABELS[detailExercise.equipment] || ""} · {PATTERN_LABELS[detailExercise.pattern]}
                  </div>
                </div>
              </div>
              <button type="button" onClick={() => setDetailExercise(null)} style={{ background: "none", border: "none", color: COLORS.steelDim }}><X size={18} /></button>
            </div>

            <div style={{ background: COLORS.surfaceAlt, borderRadius: 10, padding: 12, marginBottom: 12 }}>
              <div style={{ fontSize: 11, color: COLORS.steelDim, marginBottom: 4 }}>主に鍛えられる部位</div>
              <div style={{ fontSize: 14, fontWeight: 600 }}>{detailExercise.muscle || CATEGORY_MUSCLE[detailExercise.category] || detailExercise.category}</div>
            </div>

            <div style={{ marginBottom: 12 }}>
              <div style={{ fontSize: 11, color: COLORS.steelDim, marginBottom: 6 }}>動作の説明</div>
              <div style={{ fontSize: 14, lineHeight: 1.7 }}>{detailExerciseInfo.how}</div>
            </div>

            <div style={{ marginBottom: 6 }}>
              <div style={{ fontSize: 11, color: COLORS.steelDim, marginBottom: 6 }}>フォームのポイント</div>
              {detailExerciseInfo.tips.map((tip, i) => (
                <div key={i} style={{ display: "flex", gap: 8, fontSize: 13, lineHeight: 1.6, marginBottom: 6, color: COLORS.chalk }}>
                  <span style={{ color: COLORS.green, flexShrink: 0 }}>✓</span>
                  <span>{tip}</span>
                </div>
              ))}
            </div>

            {detailExercise.barbell && (
              <div style={{ fontSize: 11, color: COLORS.steelDim, marginTop: 10, paddingTop: 10, borderTop: `1px solid ${COLORS.line}` }}>
                🏋 この種目はシャフト({BAR_WEIGHT}kg)を使うバーベル種目です
              </div>
            )}
          </div>
        </div>
      )}

      {/* workout completion summary */}
      {completionSummary && (
        <div
          style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.7)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 30, padding: 24 }}
          onClick={() => setCompletionSummary(null)}
        >
          <div onClick={(e) => e.stopPropagation()} style={{ background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 16, padding: 24, width: "100%", maxWidth: 320, textAlign: "center" }}>
            <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 18, marginBottom: 4 }}>お疲れ様でした!</div>
            <div style={{ fontSize: 11, color: COLORS.steelDim, marginBottom: 18 }}>今日のワークアウト結果</div>

            <div style={{ background: COLORS.surfaceAlt, borderRadius: 12, padding: 18, marginBottom: 14 }}>
              <div style={{ fontSize: 11, color: COLORS.steelDim, marginBottom: 4 }}>推定消費カロリー</div>
              <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 32, fontWeight: 700, color: COLORS.red }}>
                {completionSummary.calories.toFixed(0)}<span style={{ fontSize: 15, marginLeft: 4 }}>kcal</span>
              </div>
            </div>

            <div style={{ display: "flex", gap: 10, marginBottom: 18 }}>
              <div style={{ flex: 1, background: COLORS.surfaceAlt, borderRadius: 10, padding: 12 }}>
                <div style={{ fontSize: 10, color: COLORS.steelDim, marginBottom: 2 }}>総重量</div>
                <div style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 15 }}>{completionSummary.volume.toLocaleString()}kg</div>
              </div>
              <div style={{ flex: 1, background: COLORS.surfaceAlt, borderRadius: 10, padding: 12 }}>
                <div style={{ fontSize: 10, color: COLORS.steelDim, marginBottom: 2 }}>種目数</div>
                <div style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 15 }}>{completionSummary.exerciseCount}種目</div>
              </div>
            </div>

            <div style={{ fontSize: 10, color: COLORS.steelDim, marginBottom: 14 }}>
              ※体重{bodyWeightKg}kgとMETs法による概算値です(挙上中・休憩中の時間を含む)
            </div>

            <button type="button" onClick={() => setCompletionSummary(null)} style={{ width: "100%", background: COLORS.red, border: "none", borderRadius: 10, padding: 12, color: "#fff", fontSize: 14, fontWeight: 600 }}>
              閉じる
            </button>
          </div>
        </div>
      )}

      {/* bottom nav */}
      <div style={{ position: "fixed", bottom: 0, left: 0, right: 0, maxWidth: 480, margin: "0 auto", background: COLORS.surface, borderTop: `1px solid ${COLORS.line}`, display: "flex", zIndex: 5 }}>
        {[
          { id: "workout", label: "ワークアウト", icon: Dumbbell },
          { id: "history", label: "履歴", icon: History },
          { id: "exercises", label: "種目", icon: ListChecks },
          { id: "profile", label: "プロフィール", icon: User },
        ].map(({ id, label, icon: Icon }) => (
          <button type="button" className="btn-tactile"
            key={id} onClick={() => setTab(id)}
            style={{ position: "relative", flex: 1, background: "none", border: "none", padding: "12px 0 10px", display: "flex", flexDirection: "column", alignItems: "center", gap: 3, color: tab === id ? COLORS.red : COLORS.steelDim }}
          >
            {tab === id && <div style={{ position: "absolute", top: 0, left: "30%", right: "30%", height: 2, background: COLORS.red, boxShadow: `0 0 8px ${COLORS.red}`, borderRadius: 2 }} />}
            <Icon size={18} style={tab === id ? { filter: `drop-shadow(0 0 4px rgba(214,64,43,0.7))` } : undefined} />
            <span style={{ fontSize: 10 }}>{label}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
