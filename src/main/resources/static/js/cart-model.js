/** Normalises a CartResponse so pages don't depend on exact field names. */
export function normalizeCart(c) {
  const items = (c?.items ?? []).map(i => {
    const price = Number(i.price ?? i.unitPrice ?? i.product?.price ?? 0);
    const qty = Number(i.quantity ?? 1);
    const stock = i.stock ?? i.availableStock ?? i.product?.stock;
    return {
      id: i.id,
      productId: i.productId ?? i.product?.id,
      name: i.productName ?? i.name ?? i.product?.name ?? 'Product',
      imageUrl: i.imageUrl ?? i.product?.imageUrl ?? '',
      price,
      qty,
      stock: stock == null ? null : Number(stock),
      subtotal: Number(i.subtotal ?? i.lineTotal ?? price * qty),
    };
  });
  const total = Number(c?.total ?? c?.totalAmount ?? c?.subtotal ?? items.reduce((s, i) => s + i.subtotal, 0));
  const count = items.reduce((s, i) => s + i.qty, 0);
  return { items, total, count };
}