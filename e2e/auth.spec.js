import { test, expect } from '@playwright/test';
const password = 'browser-only-long-password-4830';
let sequence = 0;
const account = () => `e2e_${Date.now()}_${++sequence}`;

// エミュレーションではiPhoneのキーボードによるズーム自体は再現できない。
// 実際の各画面で、自動ズームの原因になる16px未満の入力文字が戻ることを防ぐ。
test('ログイン後の全入力欄がスマホ幅でも16px以上で、手動ズームを制限しない', async ({ page }) => {
  await register(page, account());
  async function checkFields(count) {
    await expect(page.locator('input, select, textarea')).toHaveCount(count);
    for (const width of [320, 390, 430, 1280]) {
      await page.setViewportSize({ width, height: 900 });
      const fields = await page.locator('input, select, textarea').evaluateAll(elements =>
        elements.map(element => ({
          name: element.getAttribute('aria-label') || element.getAttribute('placeholder'),
          size: parseFloat(getComputedStyle(element).fontSize),
        })));
      for (const field of fields) expect(field.size, `${width}px: ${field.name}`).toBeGreaterThanOrEqual(16);
    }
  }
  await page.getByRole('button', { name: 'プロフィール', exact: true }).click();
  await expect(page.getByLabel('新しいパスワード（確認）', { exact: true })).toBeVisible();
  await checkFields(6);
  await page.getByRole('button', { name: '種目', exact: true }).click();
  await checkFields(3);
  await page.getByRole('button', { name: 'ワークアウト', exact: true }).click();
  await addBench(page);
  await checkFields(4);
  const viewport = await page.locator('meta[name="viewport"]').getAttribute('content');
  expect(viewport).not.toMatch(/user-scalable\s*=\s*(no|0)|maximum-scale\s*=/i);
});

async function register(page, id, name = '利用者A') {
  await page.goto('/');
  await page.getByRole('button', { name: '初めての方はアカウントを作成' }).click();
  await page.getByLabel('ログインID', { exact: true }).fill(id);
  await page.getByLabel('パスワード', { exact: true }).fill(password);
  await page.getByLabel('プロフィール名', { exact: true }).fill(name);
  await page.getByLabel('性別', { exact: true }).selectOption('male');
  await page.getByLabel('年齢', { exact: true }).fill('30');
  await page.getByLabel('体重（kg）', { exact: true }).fill('80');
  await page.getByRole('button', { name: '登録して始める' }).click();
  await expect(page.getByRole('button', { name: 'ワークアウト開始', exact: true })).toBeEnabled();
}
async function login(page, id) {
  await page.getByLabel('ログインID', { exact: true }).fill(id);
  await page.getByLabel('パスワード', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'ログイン', exact: true }).click();
  await expect(page.getByRole('button', { name: 'ログアウト', exact: true })).toBeVisible();
}
async function addBench(page) {
  await page.getByRole('button', { name: 'ワークアウト開始', exact: true }).click();
  await page.getByRole('button', { name: '種目を追加', exact: true }).click();
  await page.getByRole('button', { name: /^胸/ }).click();
  await page.getByRole('button', { name: /^バーベル/ }).click();
  await page.getByRole('button', { name: 'ベンチプレス', exact: true }).click();
  await page.getByLabel('ベンチプレスの重量(kg)', { exact: true }).fill('50');
  await page.getByLabel('ベンチプレスの回数', { exact: true }).fill('10');
  await page.getByRole('button', { name: 'ベンチプレスのセットを追加', exact: true }).click();
  await expect(page.getByText('50kg × 10回', { exact: true })).toBeVisible();
}

test('登録・途中保存・再読込・完了・再ログイン・別ユーザー分離', async ({ page }, testInfo) => {
  const a = account(), b = account();
  await register(page, a);
  await addBench(page);
  await page.reload();
  await expect(page.getByText('50kg × 10回', { exact: true })).toBeVisible();
  await page.getByLabel('ベンチプレスのメモ', { exact: true }).fill('Aだけのトレーニング記録');
  await page.getByRole('button', { name: 'ワークアウトを終了して保存', exact: true }).click();
  await expect(page.getByText('お疲れ様でした!', { exact: true })).toBeVisible();
  await page.getByRole('button', { name: '閉じる', exact: true }).click();
  await page.getByRole('button', { name: '履歴', exact: true }).click();
  await page.screenshot({ path: testInfo.outputPath('recorded-history.png'), fullPage: true });
  await page.getByRole('button', { name: 'ログアウト', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'ログイン', exact: true })).toBeVisible();
  await login(page, a);
  const aHistory = await page.evaluate(async () => {
    const user = await (await fetch('/api/auth/me')).json();
    return (await fetch('/api/workouts/history', { headers: { 'X-Overload-Account': user.accountKey } })).json();
  });
  expect(aHistory).toHaveLength(1);
  expect(aHistory[0].note).toBe('Aだけのトレーニング記録');
  await page.getByRole('button', { name: 'ログアウト', exact: true }).click();
  await register(page, b, '利用者B');
  await page.getByRole('button', { name: '履歴', exact: true }).click();
  await expect(page.getByText('この月の記録はありません', { exact: true })).toBeVisible();
  await page.getByRole('button', { name: 'プロフィール', exact: true }).click();
  await expect(page.getByLabel('プロフィール名', { exact: true })).toHaveValue('利用者B');
  await page.getByLabel('プロフィール名', { exact: true }).fill('Bのプロフィール');
  await expect(page.getByText('保存済み', { exact: true })).toBeVisible();
  await page.reload();
  await page.getByRole('button', { name: 'プロフィール', exact: true }).click();
  await expect(page.getByLabel('プロフィール名', { exact: true })).toHaveValue('Bのプロフィール');
  await page.screenshot({ path: testInfo.outputPath('separated-profile.png'), fullPage: true });
});

test('別タブのアカウント切替後に古い画面から保存すると拒否され、下書きを保持する', async ({ page, context }) => {
  const a = account(), b = account();
  await register(page, a); await addBench(page);
  const oldDraft = await page.evaluate(() => Object.keys(localStorage).filter(key => key.startsWith('overload:workout-draft:')));
  const second = await context.newPage();
  await second.goto('/');
  await second.getByRole('button', { name: 'ログアウト', exact: true }).click();
  await register(second, b, '利用者B');
  await page.getByRole('button', { name: 'ワークアウトを終了して保存', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'ログイン', exact: true })).toBeVisible();
  await expect(page.getByText(/ログイン状態が変わりました/)).toBeVisible();
  expect(await page.evaluate(keys => keys.every(key => localStorage.getItem(key) !== null), oldDraft)).toBe(true);
  await login(page, a);
  await expect(page.getByText('50kg × 10回', { exact: true })).toBeVisible();
  await page.getByRole('button', { name: /保存を再試行|ワークアウトを終了して保存/ }).click();
  await expect(page.getByText('お疲れ様でした!', { exact: true })).toBeVisible();
});

test('認証画面の初期表示と不正ログイン', async ({ page }, testInfo) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'ログイン', exact: true })).toBeVisible();
  await page.getByLabel('ログインID', { exact: true }).fill('unknown_account');
  await page.getByLabel('パスワード', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'ログイン', exact: true }).click();
  await expect(page.getByRole('alert')).toContainText('ログインIDまたはパスワード');
  await page.screenshot({ path: testInfo.outputPath('login-screen.png'), fullPage: true });
});
