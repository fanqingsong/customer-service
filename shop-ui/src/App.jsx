import { useCallback, useEffect, useState } from 'react';

const API_BASE = import.meta.env.VITE_ORCHESTRATOR_URL || '';

async function api(path, options) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    ...options,
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || res.statusText);
  }
  return res.json();
}

export default function App() {
  const [products, setProducts] = useState([]);
  const [sku, setSku] = useState('CAR');
  const [quantity, setQuantity] = useState(1);
  const [simulatePaymentFailure, setSimulatePaymentFailure] = useState(false);
  const [instanceId, setInstanceId] = useState('');
  const [status, setStatus] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api('/api/products')
      .then((list) => {
        setProducts(list);
        if (list.length > 0) {
          setSku(list[0].sku);
        }
      })
      .catch((e) => setError(String(e.message || e)));
  }, []);

  const refreshStatus = useCallback(async (id) => {
    if (!id) return;
    const s = await api(`/api/checkout/${id}`);
    setStatus(s);
  }, []);

  useEffect(() => {
    if (!instanceId) return undefined;
    const timer = setInterval(() => {
      refreshStatus(instanceId).catch((e) => setError(String(e.message || e)));
    }, 1500);
    return () => clearInterval(timer);
  }, [instanceId, refreshStatus]);

  const checkout = async () => {
    setError('');
    setLoading(true);
    setStatus(null);
    try {
      const started = await api('/api/checkout', {
        method: 'POST',
        body: JSON.stringify({ sku, quantity, simulatePaymentFailure }),
      });
      setInstanceId(started.instanceId);
      await refreshStatus(started.instanceId);
    } catch (e) {
      setError(String(e.message || e));
    } finally {
      setLoading(false);
    }
  };

  const selected = products.find((p) => p.sku === sku);
  const total = selected ? selected.unitPrice * quantity : 0;

  return (
    <div className="page">
      <h1>Commerce Saga（Dapr Workflow）</h1>
      <p>订单 → 库存 → 支付，失败时自动补偿（释放库存、取消订单）。</p>

      <div className="card">
        <h2>商品</h2>
        <div className="row">
          <label>
            SKU
            <select value={sku} onChange={(e) => setSku(e.target.value)}>
              {products.map((p) => (
                <option key={p.sku} value={p.sku}>
                  {p.name} ({p.sku}) — 库存 {p.stock} — ¥{p.unitPrice}
                </option>
              ))}
            </select>
          </label>
          <label>
            数量
            <input
              type="number"
              min={1}
              value={quantity}
              onChange={(e) => setQuantity(Number(e.target.value))}
            />
          </label>
          <span>合计：¥{total}</span>
        </div>
        <div className="row" style={{ marginTop: '1rem' }}>
          <label>
            <input
              type="checkbox"
              checked={simulatePaymentFailure}
              onChange={(e) => setSimulatePaymentFailure(e.target.checked)}
            />
            模拟支付失败（触发 Saga 补偿）
          </label>
          <button type="button" disabled={loading || !sku} onClick={checkout}>
            {loading ? '提交中…' : '下单'}
          </button>
        </div>
      </div>

      {instanceId && (
        <div className="card">
          <h2>Workflow 状态</h2>
          <p>
            Instance ID: <code>{instanceId}</code>
          </p>
          {status && (
            <>
              <p>Runtime: {status.runtimeStatus}</p>
              <pre>{JSON.stringify(status, null, 2)}</pre>
            </>
          )}
        </div>
      )}

      {error && (
        <div className="card">
          <strong style={{ color: '#b91c1c' }}>{error}</strong>
        </div>
      )}
    </div>
  );
}
