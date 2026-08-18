import dayjs from 'dayjs';

export function formatDateTime(dateStr) {
  if (!dateStr) {
    return '-';
  }
  const date = dayjs(dateStr);
  if (!date.isValid()) {
    return '-';
  }
  return {
    date: date.format('YYYY-MM-DD'),
    time: date.format('HH:mm:ss')
  };
}

export function formatDateTimeSimple(dateStr) {
  if (!dateStr) {
    return '-';
  }
  const date = dayjs(dateStr);
  if (!date.isValid()) {
    return '-';
  }
  return `${date.format('YYYY-MM-DD')}\n${date.format('HH:mm:ss')}`;
}

export function formatDateTimeHtml(dateStr) {
  if (!dateStr) {
    return '-';
  }
  const date = dayjs(dateStr);
  if (!date.isValid()) {
    return '-';
  }
  return `<div style="white-space: pre-line; line-height: 1.4;">${date.format('YYYY-MM-DD')}\n${date.format('HH:mm:ss')}</div>`;
}
